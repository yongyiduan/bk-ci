export default {
    callBack: () => {},

    installWsMessage (callBack, key) {
        this.callBack = (res = {}) => {
            const { webSocketType, module, page, message } = res.data || {}
            const wsKey = webSocketType + module
            if (wsKey === key && location.href.includes(page)) {
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
