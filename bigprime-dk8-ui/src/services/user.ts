import request from '@/utils/request'


/**
 * 用户信息
 */
export interface UserInfo {
    id: number,
    username: string,
    realName: string,
    avatar: string,
    avatarUrl: string,
    gender: number,
    email: string,
    mobile: string,
    orgId: number,
    superAdmin: number,
    status: number,
    roleIdList: number[],
    orgName: string,
    createTime: string,
    dinkyUrl: string
}

/**
 * 用户
 */
export namespace UserService {

    export const login = async (param: {}) => {
        return request.post('/auth/login', param)
    }

    export const logout = async () => {
        const res = await request.post('/auth/logout')
        return res.data as boolean
    }

    export const checkToken = async (token: string) => {
        return request.post('/auth/check-token/' + token)
    }
}




