<template>
    <article class="gitci-main" v-bkloading="{ isLoading }">
        <header class="gitci-header">
            <span class="header-info">
                <!-- <icon name="devops" size="30"></icon>
                <span class="ci-name" @click="goToHome">蓝盾 | Stream</span> -->
                <img class="ci-name" src="./images/logo.svg" height="48" />
                <template v-if="$route.hash">
                    <icon name="git" size="18" class="gray-icon"></icon>
                    <span class="git-project-path" @click="goToCode">{{ decodeURIComponent(($route.hash || '').slice(1)) }}</span>
                    <icon
                        name="setting"
                        size="18"
                        :class="computedIconClass"
                        @click.native="goToSetting"
                        v-bk-tooltips="{ content: 'Permission denied', disabled: permission }"
                    ></icon>
                </template>
            </span>
            <section class="user-info">
                <img :src="user.avatarUrl" class="avatar" />
                <bk-dropdown-menu align="right" trigger="click">
                    <div class="dropdown-trigger-btn" slot="dropdown-trigger">
                        <span class="name">{{ user.username }}</span>
                        <span class="unread" v-if="messageNum > 0"></span>
                        <i class="bk-icon icon-down-shape"></i>
                    </div>
                    <ul class="bk-dropdown-list" slot="dropdown-content">
                        <li :class="{ unread: messageNum > 0 }"><a href="javascript:;" @click="goToNotifications">Notifications</a></li>
                        <li><a href="javascript:;" @click="goLogin">Login Out</a></li>
                    </ul>
                </bk-dropdown-menu>
                <a href="https://iwiki.woa.com/x/klPpK" target="_blank"><i class="bk-icon icon-question-circle-shape"></i></a>
            </section>
        </header>

        <router-view class="gitci-content" v-if="!isLoading" :name="childRouteName"></router-view>
    </article>
</template>

<script>
    import { common, notifications, pipelines } from '@/http'
    import { mapActions, mapState } from 'vuex'
    import gitCiWebSocket from '@/utils/websocket'
    import register from '@/utils/websocket-register'

    export default {
        name: 'app',

        data () {
            return {
                messageNum: 0,
                isLoading: false
            }
        },

        computed: {
            ...mapState(['exceptionInfo', 'projectInfo', 'projectId', 'user', 'permission']),

            computedIconClass () {
                const name = this.$route.name
                const settingPages = ['setting', 'basicSetting', 'credentialList', 'agentPools', 'addAgent', 'agentList', 'agentDetail']
                const iconColor = settingPages.includes(name) ? 'blue-icon' : 'gray-icon'
                return [iconColor, 'setting']
            },

            childRouteName () {
                return this.exceptionInfo.type === 200 ? 'default' : 'exception'
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
                Promise.all([this.getUserInfo(), this.getProjectInfo()]).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                })
            },

            getUserInfo () {
                return common.getUserInfo().then((userInfo = {}) => {
                    this.setUser(userInfo)
                })
            },

            getProjectInfo () {
                if (window.location.pathname && window.location.pathname.startsWith('/home')) return
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
            },

            goToSetting () {
                if (!this.permission) return

                this.$router.push({ name: 'basicSetting' })
            },

            goLogin () {
                const loginUrl = new URL(LOGIN_SERVICE_URL)
                loginUrl.searchParams.append('c_url', location.href)
                window.location.href = loginUrl.href
            },

            goToNotifications () {
                this.$router.push({ name: 'notifications' })
            },

            goToCode () {
                window.open(this.projectInfo.web_url, '_blank')
            },

            goToHome () {
                const pipelineId = this.$route.params.pipelineId
                this.$router.push({
                    name: 'buildList',
                    params: {
                        pipelineId
                    }
                })
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

    .gitci-header {
        height: 61px;
        padding: 0 20px 0 10px;
        background: #182132;
        /* border-bottom: 1px solid #dde4eb; */
        display: flex;
        align-items: center;
        justify-content: space-between;
        color: #f5f7fa;
        .header-info {
            display: flex;
            justify-content: center;
            align-items: center;
            .ci-name {
                display: inline-block;
                margin: 0 40px 0 12px;
                font-size: 16px;
                color: #f5f7fa;
                cursor: pointer;
            }
            .git-project-path {
                display: inline-block;
                margin: 0 8px;
                color: #f5f7fa;
                cursor: pointer;
            }
            .setting {
                cursor: pointer;
            }
            .gray-icon {
                color: #979ba5;
                font-weight: normal;
            }
            .blue-icon {
                color: #3a84ff;
            }
        }
    }

    .gitci-content {
        height: calc(100% - 61px);
        overflow: auto;
    }

    .bk-dropdown-list li {
        min-width: 65px;
        position: relative;
    }
    .unread:before {
        content: '';
        position: absolute;
        right: 16px;
        top: calc(50% - 3px);
        width: 8px;
        height: 8px;
        border-radius: 100px;
        background: #ff5656;
    }

    .dropdown-trigger-btn {
        cursor: pointer;
        font-size: 14px;
        .name {
            color: #f5f7fa;
            margin: 0 8px;
        }
    }

    .user-info {
        display: flex;
        align-items: center;
        .avatar {
            width: 25px;
            height: 25px;
            border-radius: 100px;
            margin-right: 5px;
        }
        a {
            color: #fff;
        }
        .icon-question-circle-shape {
            cursor: pointer;
            margin-left: 12px;
        }
    }
</style>
