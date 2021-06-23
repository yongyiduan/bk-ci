export default {
    callBack: () => {},

    installWsMessage (callBack, type) {
        this.callBack = (res) => {
            const data = res.data
            const webSocketType = data.webSocketType
            if (webSocketType === type) {
                const message = JSON.parse(data.message)
                callBack(message)
            }
        }
        window.addEventListener('message', this.callBack)
    },

    unInstallWsMessage () {
        window.removeEventListener('message', this.callBack)
        this.callBack = () => {}
    }
}
