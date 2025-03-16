package com.bigprime.dk8.common.config.websocket;

import com.bigprime.dk8.common.config.kubernetes.K8sClientProvider;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.kubernetes.client.openapi.ApiClient;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SSHWebSocketHandler extends TextWebSocketHandler {

    private final K8sClientProvider clientProvider;

    public SSHWebSocketHandler(K8sClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    private enum AuthState {CONNECT, INIT, USERNAME, PUTHWORD, AUTHENTICATED, EXEC}

    private static class SessionContext {
        AuthState state = AuthState.CONNECT;
        String host;
        int port;
        String username;
        String password;
        SshClient client;
        ClientSession session;
        ChannelShell channel;
    }

    private static final Map<WebSocketSession, SessionContext> sessions = new ConcurrentHashMap<>();

    private SessionContext getOrCreateContext(WebSocketSession session) {
        return sessions.computeIfAbsent(session, k -> new SessionContext());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        SessionContext ctx = getOrCreateContext(session);
        ctx.client = SshClient.setUpDefaultClient();
        ctx.client.start();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        SessionContext ctx = getOrCreateContext(session);
        String input = message.getPayload().trim();

        switch (ctx.state) {
            case CONNECT:
                handleConnectInput(session, ctx, message);
                break;
            case INIT:
                handleUsernameInput(session, ctx, input);
                break;
            case USERNAME:
                handlePasswordInput(session, ctx, input);
                break;
            case AUTHENTICATED:
                forwardCommandToSSH(ctx.channel, input);
                break;
            case EXEC:
                podExecToSSH(session, ctx, message);
                break;
        }
    }

    private void handleConnectInput(WebSocketSession session, SessionContext ctx, TextMessage message) {
        String payload = message.getPayload();
        JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
        String host = "";
        if (json.has("host")) {
            host = json.get("host").getAsString();
        }
        if (host.equals("") || host.isEmpty()) {
            try {
                ApiClient apiClient = clientProvider.apiClient();
                String path = apiClient.getBasePath();
                URL url = new URL(path);
                host = url.getHost();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (host.equals("") || host.isEmpty()) {
            throw new RuntimeException("miss Host parameter");
        }
        ctx.host = host;

        if (json.has("port")) {
            ctx.port = json.get("port").getAsInt();
        } else {
            ctx.port = 22;
        }


        sendPrompt(session, "login: ");
        ctx.state = AuthState.INIT;
    }

    private void handleUsernameInput(WebSocketSession session, SessionContext ctx, String username) {
        ctx.username = username;
        ctx.state = AuthState.USERNAME;
        sendPrompt(session, "password: ");
    }

    private void handlePasswordInput(WebSocketSession session, SessionContext ctx, String password) {
        try {
            // 建立SSH连接
            ctx.session = ctx.client.connect(ctx.username, ctx.host, ctx.port)
                    .verify(15000)
                    .getSession();

            ctx.session.addPasswordIdentity(password);
            ctx.session.auth().verify(15000);
            // 创建Shell通道
            ctx.channel = ctx.session.createShellChannel();
            setupChannelIO(session, ctx.channel);
            ctx.channel.open().verify(15000);
            ctx.state = AuthState.AUTHENTICATED;
            session.sendMessage(new TextMessage("\r\n$ "));
            ctx.channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TimeUnit.SECONDS.toMillis(3));
        } catch (Exception e) {
            handleError(session, "Authentication failed: " + e.getMessage());
            resetContext(session);
        }
    }

    private void podExecToSSH(WebSocketSession session, SessionContext ctx, TextMessage message) throws IOException {
        String payload = message.getPayload();
        JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
        ctx.host = json.get("host").getAsString();
        ctx.port = json.get("port").getAsInt();
        ctx.username = json.get("username").getAsString();
        ctx.password = json.get("password").getAsString();
        String pod = json.get("pod").getAsString();
        String namespace = json.get("namespace").getAsString();
        ctx.session = ctx.client.connect(ctx.username, ctx.host, ctx.port)
                .verify(15000)
                .getSession();

        ctx.session.addPasswordIdentity(ctx.password);
        ctx.session.auth().verify(15000);
        // 创建Shell通道
        ctx.channel = ctx.session.createShellChannel();
        setupChannelIO(session, ctx.channel);
        ctx.channel.open().verify(15000);
        ctx.state = AuthState.AUTHENTICATED;
        session.sendMessage(new TextMessage("kubectl exec -it " + pod + " -n " + namespace + " -- /bin/bash "));
        ctx.channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TimeUnit.SECONDS.toMillis(3));
    }

    private void handleError(WebSocketSession session, String message) {
        try {
            session.sendMessage(new TextMessage("\r\nERROR: " + message + "\r\n"));
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException e) {
            // Ignore close errors
        }
    }

    private void forwardCommandToSSH(ChannelShell channel, String command) {
        if (channel.isOpen()) {
            OutputStream out = channel.getInvertedIn();
            if (out != null) {
                try {
                    out.write((command + "\n").getBytes());
                    out.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void setupChannelIO(WebSocketSession wsSession, ChannelShell channel) {
        // 处理SSH输出
        channel.setOut(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                //wsSession.sendMessage(new TextMessage(new byte[]{(byte) b}));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                wsSession.sendMessage(new TextMessage(new String(b, off, len)));
            }
        });

        // 处理错误流
        channel.setErr(channel.getOut());
    }

    private void sendPrompt(WebSocketSession session, String prompt) {
        try {
            session.sendMessage(new TextMessage(prompt));
        } catch (IOException e) {
            handleError(session, "Failed to send prompt");
        }
    }

    private void resetContext(WebSocketSession session) {
        SessionContext ctx = sessions.get(session);
        if (ctx != null) {
            try {
                if (ctx.channel != null) ctx.channel.close();
                if (ctx.session != null) ctx.session.close();
                if (ctx.client != null) ctx.client.stop();
            } catch (IOException e) {
                // Ignore close errors
            }
            sessions.remove(session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        resetContext(session);
    }
}