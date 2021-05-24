<template>
    <article class="gitci-main" v-bkloading="{ isLoading }">
        <header class="gitci-header">
            <span class="header-info">
                <icon name="export-ci" size="44"></icon>
                <span class="ci-name">Tencent CI</span>
                <template v-if="$route.hash">
                    <icon name="git" size="18" class="gray-icon"></icon>
                    <span class="git-project-path">{{ `${decodeURIComponent(($route.hash || '').slice(1))}` }}</span>
                    <icon name="setting" size="18" :class="computedIconClass" @click.native="goToSetting"></icon>
                </template>
            </span>
            <section class="user-info">
                <img :src="userInfo.img" class="avatar" />
                <bk-dropdown-menu align="right">
                    <div class="dropdown-trigger-btn" slot="dropdown-trigger">
                        <span class="name">{{ userInfo.name }}</span>
                        <span class="unread" v-if="messageNum > 0"></span>
                        <i class="bk-icon icon-down-shape"></i>
                    </div>
                    <ul class="bk-dropdown-list" slot="dropdown-content">
                        <li :class="{ unread: messageNum > 0 }"><a href="javascript:;" @click="goToNotifications">通知</a></li>
                        <li><a href="javascript:;" @click="goLogin">退出</a></li>
                    </ul>
                </bk-dropdown-menu>
            </section>
        </header>

        <router-view class="gitci-content" v-if="!isLoading" :name="childRouteName"></router-view>
    </article>
</template>

<script>
    import { common, notifications } from '@/http'
    import { mapActions, mapState } from 'vuex'

    export default {
        name: 'app',

        data () {
            return {
                userInfo: {
                    name: '',
                    img: ''
                },
                messageNum: 0,
                isLoading: false
            }
        },

        computed: {
            ...mapState(['exceptionInfo']),

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
                handler () {
                    this.initData()
                },
                immediate: true
            }
        },

        beforeDestroy () {
            clearTimeout(this.loopGetNotifications.loopId)
        },

        methods: {
            ...mapActions(['setProjectInfo', 'setExceptionInfo']),

            initData () {
                this.isLoading = true
                Promise.all([this.getUserInfo(), this.getProjectInfo()]).then(() => {
                    return this.loopGetNotifications()
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                })
            },

            getUserInfo () {
                return common.getUserInfo().then((userInfo = {}) => {
                    this.userInfo = { name: userInfo.username, img: userInfo.avatarUrl }
                })
            },

            getProjectInfo () {
                return new Promise((resolve, reject) => {
                    const projectPath = (location.hash || '').slice(1)

                    if (projectPath) {
                        common.getProjectInfo(projectPath).then((projectInfo = {}) => {
                            if (projectInfo.id) {
                                this.setProjectInfo(projectInfo)
                            } else {
                                this.setExceptionInfo({ type: 499 })
                            }
                            resolve()
                        }).catch((err) => {
                            reject(err)
                        })
                    } else {
                        this.setExceptionInfo({ type: 520 })
                        resolve()
                    }
                })
            },

            getNotifications () {
                return notifications.getUnreadNotificationNum().then((res) => {
                    this.messageNum = res || 0
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                })
            },

            loopGetNotifications () {
                clearTimeout(this.loopGetNotifications.loopId)
                this.getNotifications()
                this.loopGetNotifications.loopId = setTimeout(this.loopGetNotifications, 5000)
            },

            goToSetting () {
                this.$router.push({ name: 'basicSetting' })
            },

            goLogin () {
                const cUrl = location.origin + location.pathname + encodeURIComponent(location.search)
                const loginUrl = new URL(LOGIN_SERVICE_URL)
                loginUrl.searchParams.append('c_url', cUrl)
                window.location.href = loginUrl.href
            },

            goToNotifications () {
                this.$router.push({ name: 'notifications' })
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .gitci-main {
        width: 100%;
        height: 100vh;
        overflow: hidden;
        font-size: 14px;
        color: #7b7d8a;
        background: #f5f6fa;
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
        padding: 0 20px;
        background: #182132;
        border-bottom: 1px solid #dde4eb;
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
                margin: 0 40px 0 7.8px;
                font-size: 20px;
                color: #f5f7fa;
            }
            .git-project-path {
                display: inline-block;
                margin: 0 8px;
                color: #f5f7fa;
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
        }
    }
</style>
