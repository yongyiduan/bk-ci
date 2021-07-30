<template>
    <article class="gitci-main" v-bkloading="{ isLoading }">
        <gitci-header></gitci-header>

        <router-view class="gitci-content" v-if="!isLoading" :name="childRouteName"></router-view>
    </article>
</template>

<script>
    import { common, notifications, pipelines } from '@/http'
    import { mapActions, mapState } from 'vuex'
    import gitCiWebSocket from '@/utils/websocket'
    import register from '@/utils/websocket-register'
    import gitciHeader from '@/components/header'

    export default {
        name: 'app',

        components: {
            gitciHeader
        },

        data () {
            return {
                messageNum: 0,
                isLoading: false
            }
        },

        computed: {
            ...mapState(['exceptionInfo', 'projectInfo', 'projectId', 'permission']),

            childRouteName () {
                return this.exceptionInfo.type === 200 ? 'default' : 'exception'
            },

            showMenu () {
                return window.location.pathname && (window.location.pathname.startsWith('/home') || window.location.pathname.startsWith('/dashboard'))
            }
        },

        watch: {
            '$route.hash': {
                handler (val) {
                    if (val) {
                        this.initData()
                    }
                }
            }
        },

        created () {
            this.initData()
        },

        beforeDestroy () {
            register.unInstallWsMessage('notify')
        },

        methods: {
            ...mapActions(['setProjectInfo', 'setExceptionInfo', 'setPermission', 'setUser']),

            initData () {
                this.isLoading = true
                Promise.all([this.getProjectInfo()]).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                })
            },

            getProjectInfo () {
                if (this.showMenu) return
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
                    this.messageNum = res || 0
                })
            },

            loopGetNotifications () {
                this.getNotifications()
                register.installWsMessage(this.getNotifications, 'NOTIFYgitci', 'notify')
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .gitci-main {
        width: 100%;
        height: 100vh;
        overflow: auto;
        font-size: 14px;
        /* color: #7b7d8a; */
        background: #f5f5f5;
        font-family: -apple-system,PingFang SC,BlinkMacSystemFont,Microsoft YaHei,Helvetica Neue,Arial;
        ::-webkit-scrollbar-thumb {
            background-color: #c4c6cc !important;
            border-radius: 3px !important;
            &:hover {
                background-color: #979ba5 !important;
            }
        }
        ::-webkit-scrollbar {
            width: 6px !important;
            height: 6px !important;
        }
    }

    .gitci-content {
        height: calc(100% - 61px);
        overflow: auto;
    }
</style>
