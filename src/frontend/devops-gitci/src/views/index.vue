<template>
    <article class="gitci-main" v-bkloading="{ isLoading }">
        <header class="gitci-header">
            <span class="header-info">
                <icon name="export-ci" size="44"></icon>
                <span class="ci-name">Tencent CI</span>
                <template v-if="$route.params.workspace && $route.params.projectPath">
                    <icon name="git" size="18" class="gray-icon"></icon>
                    <span class="git-project-path">{{ `${$route.params.workspace}/${$route.params.projectPath}` }}</span>
                    <icon name="setting" size="18" :class="computedIconClass" @click.native="goToSetting"></icon>
                </template>
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
    import { modifyRequestCommonHead } from '@/http/ajax'

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

        computed: {
            computedIconClass () {
                const name = this.$route.name
                const settingPages = ['setting', 'basicSetting', 'credentialList', 'agentPools', 'addAgent', 'agentList', 'agentDetail']
                const iconColor = settingPages.includes(name) ? 'blue-icon' : 'gray-icon'
                return [iconColor, 'setting']
            }
        },

        created () {
            this.initData()
        },

        methods: {
            ...mapActions(['setProjectInfo']),

            initData () {
                this.isLoading = true
                const projectPath = this.$route.params.workspace + '/' + this.$route.params.projectPath
                Promise.all([common.getUserInfo(), common.getProjectInfo(projectPath)]).then(([userInfo, projectInfo]) => {
                    const data = userInfo || {}
                    this.userInfo = {
                        name: data.username,
                        img: data.avatarUrl
                    }
                    const projectData = projectInfo || {}
                    if (projectData.id) {
                        const projectId = `git_${projectData.id}`
                        this.setProjectInfo(projectData)
                        modifyRequestCommonHead({ 'X-DEVOPS-PROJECT-ID': projectId })
                    } else {
                        this.failGetProjectInfo(499)
                    }
                }).catch((err) => {
                    this.failGetProjectInfo(500)
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                })
            },

            failGetProjectInfo (type) {
                if (this.$route.name !== 'exception') {
                    this.$router.push({ path: `/exception/${type}`, params: { type } })
                }
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
