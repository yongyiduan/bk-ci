<template>
    <router-view v-if="!isLoading"></router-view>
</template>

<script>
    import { common, notifications, pipelines } from '@/http'
    import { mapActions, mapState } from 'vuex'
    import gitCiWebSocket from '@/utils/websocket'
    import register from '@/utils/websocket-register'

    export default {
        data () {
            return {
                isLoading: false
            }
        },
        computed: {
            ...mapState(['exceptionInfo', 'projectInfo', 'projectId', 'permission'])
        },
        created () {
            console.log('project-index')
            this.initData()
        },
        beforeDestroy () {
            register.unInstallWsMessage('notify')
        },

        methods: {
            ...mapActions(['setProjectInfo', 'setExceptionInfo', 'setPermission', 'setMessageNum']),

            initData () {
                this.isLoading = true
                Promise.all([this.getProjectInfo()]).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                })
            },

            getProjectInfo () {
                return new Promise((resolve, reject) => {
                    const projectPath = (location.hash || '').slice(1)

                    if (projectPath) {
                        common.getProjectInfo(projectPath).then((projectInfo = {}) => {
                            if (projectInfo.id) {
                                this.setProjectInfo(projectInfo)
                                this.loopGetNotifications()
                                this.getPermission()
                                this.setExceptionInfo({ type: 200 })
                                gitCiWebSocket.changeRoute(this.$route)
                            } else {
                                this.setExceptionInfo({ type: 499 })
                            }
                            resolve()
                        }).catch((err) => {
                            resolve()
                            this.setExceptionInfo({ type: 500, message: err.message || err })
                        })
                    } else {
                        this.setExceptionInfo({ type: 520 })
                        resolve()
                    }
                })
            },

            getPermission () {
                return pipelines.requestPermission(this.projectId).then((res) => {
                    this.setPermission(res)
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                })
            },

            getNotifications () {
                return notifications.getUnreadNotificationNum(this.projectId).then((res) => {
                    this.setMessageNum(res || 0)
                })
            },

            loopGetNotifications () {
                this.getNotifications()
                register.installWsMessage(this.getNotifications, 'NOTIFYgitci', 'notify')
            }
        }
    }
</script>
