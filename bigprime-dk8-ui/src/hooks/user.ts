import {useRouter} from 'vue-router'
import {useI18n} from 'vue-i18n';
import {Modal} from '@opentiny/vue';
import {useUserStore} from '@/store';

export default function useUser() {
    const {t} = useI18n();
    const router = useRouter();
    const userStore = useUserStore();
    const logout = async () => {
        await userStore.logout();
        Modal.message({
            message: t('setting.loginout'),
            status: 'success',
        });
        await router.push({
            name: 'login'
        });
    };
    return {
        logout,
    };
}
