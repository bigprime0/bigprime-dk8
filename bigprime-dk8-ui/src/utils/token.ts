/**
 * token 操作封装
 */
import {TOKEN_STORE_NAME, USER_ID, DOMAINS} from '@/config/setting'

/**
 * 获取缓存的 token
 */
export const getToken = () => {
    const token = localStorage.getItem(TOKEN_STORE_NAME)
    if (!token) {
        return sessionStorage.getItem(TOKEN_STORE_NAME)
    }
    return token
}

/**
 * 获取缓存的用户ID
 */
export const getUserId = () => {
    const userId = localStorage.getItem(USER_ID)
    if (!userId) {
        return sessionStorage.getItem(USER_ID)
    }
    return userId
}


export const getDomain = (type: any) => {
    let domains: any = localStorage.getItem(DOMAINS)
    if (!domains) {
        domains =  sessionStorage.getItem(DOMAINS)
    }
    if(!domains){
        return ''
    }
    return JSON.parse(domains)[type]
}


/**
 * 缓存 token
 * @param token token
 * @param userId 用户ID
 * @param remember 是否永久存储
 */
export const setToken = (token: string, userId: string, remember: boolean, domains: {}) => {
    removeToken()
    if (token) {
        if (remember) {
            localStorage.setItem(TOKEN_STORE_NAME, token)
            localStorage.setItem(USER_ID, userId)
            localStorage.setItem(DOMAINS, JSON.stringify(domains))
        } else {
            sessionStorage.setItem(TOKEN_STORE_NAME, token)
            sessionStorage.setItem(USER_ID, userId)
            sessionStorage.setItem(DOMAINS, JSON.stringify(domains))
        }
    }
}

/**
 * 移除 token
 */
export const removeToken = () => {
    localStorage.removeItem(TOKEN_STORE_NAME)
    localStorage.removeItem(USER_ID)
    localStorage.removeItem(DOMAINS)
    sessionStorage.removeItem(TOKEN_STORE_NAME)
    sessionStorage.removeItem(USER_ID)
    sessionStorage.removeItem(DOMAINS)
    sessionStorage.removeItem('ACTIVE_APP')
}
