<template>
    <header class="gitci-header">
        <span class="header-info">
            <img class="ci-name" src="./../images/logo.svg" height="48" />

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

        <div class="navigation-header" v-if="showMenu">
            <ol class="header-nav">
                <li v-for="item in menuList" :key="item.name" class="header-nav-item" :class="{ 'item-active': item.active }">
                    {{item.name}}
                </li>
            </ol>
        </div>

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
</template>

<script>
    import { mapActions, mapState } from 'vuex'
    import { common } from '@/http'

    export default ({
        name: 'gitci-header',
        props: {
            messageNum: {
                type: Number,
                default: 0
            }
        },
        data () {
            return {
            }
        },
        computed: {
            ...mapState(['exceptionInfo', 'projectInfo', 'projectId', 'user', 'permission']),
            showMenu () {
                return window.location.pathname && (window.location.pathname.startsWith('/home') || window.location.pathname.startsWith('/dashboard'))
            },
            computedIconClass () {
                const name = this.$route.name
                const settingPages = ['setting', 'basicSetting', 'credentialList', 'agentPools', 'addAgent', 'agentList', 'agentDetail']
                const iconColor = settingPages.includes(name) ? 'blue-icon' : 'gray-icon'
                return [iconColor, 'setting']
            },
            menuList () {
                return [
                    {
                        name: 'Dashboard',
                        active: window.location.pathname && window.location.pathname.startsWith('/dashboard'),
                        type: 'click',
                        func: ''
                    },
                    {
                        name: 'ChangeLog',
                        active: false,
                        type: 'url',
                        url: ''
                    },
                    {
                        name: 'Documentation',
                        active: false,
                        type: 'url',
                        url: ''
                    }
                ]
            }
        },
        created () {
            this.getUserInfo()
        },
        methods: {
            ...mapActions(['setUser']),

            getUserInfo () {
                return common.getUserInfo().then((userInfo = {}) => {
                    this.setUser(userInfo)
                })
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
    })
</script>

<style lang="postcss" scoped>
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

    .navigation-header {
        -webkit-box-flex:1;
        -ms-flex:1;
        flex:1;
        height:100%;
        display:-webkit-box;
        display:-ms-flexbox;
        display:flex;
        -webkit-box-align:center;
        -ms-flex-align:center;
        align-items:center;
        font-size:14px;
        margin-left: 100px;
    }
    .navigation-header .header-nav {
        display:-webkit-box;
        display:-ms-flexbox;
        display:flex;
        padding:0;
        margin:0;
    }
    .navigation-header .header-nav-item {
        list-style:none;
        height:50px;
        display:-webkit-box;
        display:-ms-flexbox;
        display:flex;
        -webkit-box-align:center;
        -ms-flex-align:center;
        align-items:center;
        margin-right:40px;
        color:#96A2B9;
        min-width:56px
    }
    .navigation-header .header-nav-item.item-active {
        color:#FFFFFF !important;
    }
    .navigation-header .header-nav-item:hover {
        cursor:pointer;
        color:#D3D9E4;
    }
</style>
