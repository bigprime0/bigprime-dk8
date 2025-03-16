import {defineStore} from 'pinia'
import {type UserInfo, UserService} from '@/services/user'
import {removeToken, setToken} from '@/utils/token'
import {useRouterStore} from '@/store/modules/router'


const useUserStore = defineStore('user', {
    state: () => {
        return {
            userInfo: {} as UserInfo
        }
    },

    actions: {
        async login(param: {}) {
            try {
                const res = await UserService.login(param)
                const {token, userInfo, domains} = res.data
                setToken(token, userInfo.id, true, domains)
                this.$patch({userInfo: userInfo})
            } catch (err) {
                removeToken()
                throw err
            }
        },

        logout() {
            //UserService.logout()
            this.$reset()
            const routerStore = useRouterStore()
            routerStore.resetInfo()
            removeToken()
            localStorage.removeItem('token')
        },

        async checkToken(tokenString: string) {
            const res = await UserService.checkToken(tokenString)
            if(!res.data){
                return false;
            }
            const {token, userInfo, domains} = res.data
            setToken(token, userInfo.id, true, domains)
            this.$patch({userInfo: userInfo})
            return true;
        }
    }
})

export default useUserStore

