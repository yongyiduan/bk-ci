<template>
    <article class="gitci-main">
        <header class="gitci-header">
            <span class="header-info">
                <icon name="export-ci" size="44"></icon>
                <span class="ci-name">Tencent CI</span>
                <icon name="git" size="18" class="gray-icon"></icon>
                <span class="git-project-path">{{ `${$route.params.workspace}/${$route.params.projectPath}` }}</span>
                <icon name="setting"
                    size="18"
                    :class="{ setting: true, 'gray-icon': $route.name === 'pipelines', 'blue-icon': $route.name !== 'pipelines' }"
                    @click.native="goToSetting"
                ></icon>
            </span>
            <section class="user-info">
                <img :src="userInfo.img" class="avatar" />
                <span class="name">{{ userInfo.name }}</span>
                <bk-dropdown-menu align="right">
                    <div class="dropdown-trigger-btn" slot="dropdown-trigger">
                        <i class="bk-icon icon-down-shape"></i>
                    </div>
                    <ul class="bk-dropdown-list" slot="dropdown-content">
                        <li><a href="javascript:;" @click="goToNotifications">通知</a></li>
                        <li><a href="javascript:;" @click="goLogin">退出</a></li>
                    </ul>
                </bk-dropdown-menu>
            </section>
        </header>
        <router-view class="gitci-content" v-if="!isLoading"></router-view>
    </article>
</template>

<script>
    import { common } from '@/http'
    import { mapActions } from 'vuex'

    export default {
        name: 'app',

        data () {
            return {
                userInfo: {
                    name: '',
                    img: ''
                },
                isLoading: true
            }
        },

        created () {
            this.initData()
        },

        methods: {
            ...mapActions(['setProjectId']),

            goToSetting () {
                this.$router.push({ name: 'basicSetting', params: this.$route.params })
            },

            initData () {
                this.isLoading = true
                Promise.all([common.getUserInfo(), common.getProjectId()]).then(([userInfo]) => {
                    const data = userInfo.data || {}
                    this.userInfo = {
                        name: data.username,
                        img: data.avatarUrl
                    }
                    this.setProjectId('git_10709275')
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                })
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

    .user-info {
        display: flex;
        align-items: center;
        .avatar {
            width: 25px;
            height: 25px;
            border-radius: 100px;
        }
        .name {
            color: #f5f7fa;
            margin: 0 8px;
        }
        .icon-down-shape {
            cursor: pointer;
        }
    }
</style>
