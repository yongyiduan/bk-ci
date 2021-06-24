import { getWSpath } from './index'

export default {
    callBack: () => {},

    installWsMessage (callBack, key) {
        this.callBack = (res = {}) => {
            const { webSocketType, module, page, message } = res.data || {}
            const wsKey = webSocketType + module
            const wsPath = getWSpath(location.href)
            if (wsKey === key && wsPath.includes(page)) {
                const parseMessage = JSON.parse(message)
                callBack(parseMessage)
            }
        }
        window.addEventListener('message', this.callBack)
    },

    unInstallWsMessage () {
        window.removeEventListener('message', this.callBack)
        this.callBack = () => {}
    }
}
