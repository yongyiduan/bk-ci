<template>
    <section>
        <section class="dashboard-container">
            <div class="type-container">
                <div class="navigation-header">
                    <ol class="header-nav">
                        <li v-for="item in typeList" :key="item.name" @click="changeType(item.type)" class="header-nav-item" :class="{ 'item-active': type === item.type }">
                            {{ item.name }}
                        </li>
                    </ol>
                </div>
            </div>
            <div class="content-container" v-bkloading="{ isLoading }">
                <div style="margin-bottom: 15px;">
                    <bk-input :left-icon="'bk-icon icon-search'" placeholder="Filter by name" v-model="searchStr" @enter="getRepoList(1)"></bk-input>
                </div>
                <div class="empty-repo" v-if="!repoList.length">
                    <empty-tips title="暂无项目"></empty-tips>
                </div>
                <div v-for="repo in repoList" :key="repo.id" class="repo-item">
                    <div class="repo-img">
                        <img class="img" :src="repo.avatarUrl">
                    </div>
                    <div class="repo-data">
                        <div class="repo-name">
                            {{ repo.nameWithNamespace }}
                        </div>
                        <div class="repo-desc">
                            <div v-if="repo.ciInfo && repo.ciInfo.enableCI">
                                <i :class="getIconClass(repo.ciInfo.lastBuildStatus)"></i>
                                <span class="lastest-build-info" @click="toLastBuildDetail(repo)">{{ repo.ciInfo.lastBuildMessage || 'Empty commit messages'}}</span>
                            </div>
                            <div v-else>{{ repo.description || 'Empty project description' }}</div>
                        </div>
                    </div>
                    <div class="operation">
                        <bk-popover v-if="repo.ciInfo && repo.ciInfo.enableCI" class="dot-menu" ext-cls="dot-menu-wrapper" placement="right" ref="dotMenuRef" theme="dot-menu light" :arrow="false" offset="15" :distance="0">
                            <div class="dot-menu-trigger">
                                <div class="footer-ext-dots">
                                    <div class="ext-dot"></div>
                                    <div class="ext-dot"></div>
                                    <div class="ext-dot"></div>
                                </div>
                            </div>
                            <ul class="dot-menu-list" slot="content">
                                <li @click="toProjectDetail('buildList', repo.nameWithNamespace)">pipeline</li>
                                <li @click="toProjectDetail('basicSetting', repo.nameWithNamespace)">setting</li>
                            </ul>
                        </bk-popover>
                        <bk-button v-else theme="primary" @click="enableCi(repo)">Enable CI</bk-button>
                    </div>
                </div>
            </div>
        </section>
    </section>
</template>

<script>
    import { common, setting } from '@/http'
    import { getPipelineStatusClass, getPipelineStatusCircleIconCls } from '@/components/status'
    import emptyTips from '@/components/empty-tips'
    import gitcode from './../images/home/gitcode.png'

    export default {
        components: {
            emptyTips
        },
        data () {
            return {
                isLoading: true,
                hasNext: true,
                type: 'MY_PROJECT',
                limit: 100,
                searchStr: '',
                repoList: [],
                typeList: [
                    {
                        type: 'MY_PROJECT',
                        name: 'My Projects'
                    }
                    // {
                    //     type: 'WATCHED',
                    //     name: 'Watch Projects'
                    // },
                    // {
                    //     type: 'STARRED',
                    //     name: 'Starred Projects'
                    // }
                ]
            }
        },
        created () {
            this.getRepoList()
        },
        methods: {
            getRepoList (page = 1) {
                this.isLoading = true
                common.getGitciProjects(this.type, page, this.limit, this.searchStr).then((res) => {
                    this.repoList = (res.records || []).map(item => ({
                        ...item,
                        avatarUrl: (item.avatarUrl.endsWith('.jpg') || item.avatarUrl.endsWith('.jpeg') || item.avatarUrl.endsWith('.png')) ? item.avatarUrl : gitcode
                    }))
                    this.hasNext = res.hasNext
                    this.isLoading = false
                }).catch((err) => {
                    this.$bkMessage({
                        theme: 'error',
                        message: err.message || err
                    })
                    this.isLoading = false
                })
            },
            
            changeType (type) {
                this.type = type
                this.searchStr = ''
                this.getRepoList(1)
            },

            toLastBuildDetail (repo) {
                if (repo.ciInfo.lastBuildPipelineId && repo.ciInfo.lastBuildId) {
                    this.$router.push({
                        name: 'buildDetail',
                        params: {
                            pipelineId: repo.ciInfo.lastBuildPipelineId,
                            buildId: repo.ciInfo.lastBuildId
                        },
                        hash: `#${repo.nameWithNamespace}`
                    })
                } else {
                    this.toProjectDetail('buildList', repo.nameWithNamespace)
                }
            },

            toProjectDetail (routeName, projectId) {
                this.$router.push({
                    name: routeName,
                    hash: `#${projectId}`
                })
            },
            
            enableCi (item) {
                setting.toggleEnableCi(true, {
                    id: item.id,
                    name: item.name,
                    name_with_namespace: item.nameWithNamespace,
                    https_url_to_repo: item.httpsUrlToRepo,
                    http_url_to_repo: item.httpsUrlToRepo.replace('https://', 'http://'),
                    web_url: item.webUrl
                }).then(res => {
                    console.log(res)
                    this.getRepoList(1)
                    this.$bkMessage({
                        theme: 'success',
                        message: 'EnableCI successfully'
                    })
                }).catch((err) => {
                    this.$bkMessage({
                        theme: 'primary',
                        message: err.message || err
                    })
                })
            },

            getIconClass (status) {
                return [getPipelineStatusClass(status), ...getPipelineStatusCircleIconCls(status)]
            }
        }
    }
</script>

<style lang="postcss">
    @import '@/css/conf';

    .dashboard-container {
        margin: 30px;
        .type-container {
            display: flex;
            align-items: center;
            background: #fff;
            height: 48px;

            .navigation-header {
                -webkit-box-flex: 1;
                -ms-flex: 1;
                flex: 1;
                height: 100%;
                display: -webkit-box;
                display: -ms-flexbox;
                display: flex;
                -webkit-box-align: center;
                -ms-flex-align: center;
                align-items: center;
                font-size: 14px;
                color: rgba(0,0,0,0.60);
                margin-left: 30px;
            }
            .navigation-header .header-nav {
                display: -webkit-box;
                display: -ms-flexbox;
                display: flex;
                padding: 0;
                margin: 0;
            }
            .navigation-header .header-nav-item {
                list-style: none;
                height: 50px;
                display: -webkit-box;
                display: -ms-flexbox;
                display: flex;
                -webkit-box-align: center;
                -ms-flex-align: center;
                align-items: center;
                margin-right: 40px;
                min-width: 56px
            }
            .navigation-header .header-nav-item.item-active {
                color:#3a84ff !important;
                border-bottom: 2px solid #3a84ff;
            }
            .navigation-header .header-nav-item:hover {
                cursor:pointer;
                color:#3a84ff;
            }
        }

        .content-container {
            margin-top: 15px;
            .empty-repo {
                height: 100%;
                background: #fff;
                display: flex;
            }
            .repo-item {
                height: 88px;
                width: 100%;
                background: #fff;
                display: flex;
                align-items: center;
                margin-bottom: 10px;
                padding: 0 24px;
                cursor: default;
                .repo-img {
                    .img {
                        width: 56px;
                        height: 56px;
                        border-radius: 28px;
                    }
                }
                .repo-data {
                    flex: 1;
                    margin: 0 16px;
                    .repo-name {
                        margin-bottom: 6px;
                        font-size: 16px;
                        color: rgba(0,0,0,0.90);
                    }
                    .repo-desc {
                        font-size: 14px;
                        color: #96A2B9;
                        .lastest-build-info {
                            cursor: pointer;
                        }
                        .bk-icon {
                            font-size: 20px;
                            &.executing {
                                font-size: 14px;
                            }
                            &.icon-exclamation, &.icon-exclamation-triangle, &.icon-clock {
                                font-size: 24px;
                            }
                            &.running {
                                color: #459fff;
                            }
                            &.canceled {
                                color: #f6b026;
                            }
                            &.danger {
                                color: #ff5656;
                            }
                            &.success {
                                color: #34d97b;
                            }
                            &.pause {
                                color: #ff9801;
                            }
                        }
                    }
                }
                .operation {
                    width: 120px;
                    .ext-dot {
                        width: 3px;
                        height: 3px;
                        border-radius: 50%;
                        background-color: $fontWeightColor;
                        & + .ext-dot {
                            margin-top: 4px;
                        }
                    }
                    .dot-menu-trigger {
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        width: 23px;
                        height: 100%;
                        text-align: center;
                        font-size: 0;
                        cursor: pointer;
                    }
                }
            }
        }
    }

    .dot-menu-wrapper {
        .tippy-tooltip {
            padding: 0 ;
        }
    }

    .dot-menu-list {
        padding: 4px 0;
        > li {
            font-size: 12px;
            line-height: 32px;
            text-align: left;
            padding: 0 20px;
            cursor: pointer;
            a {
                color: $fontColor;
                display: block;
            }
            &:hover {
                color: $primaryColor;
                background-color: #EAF3FF;
                a {
                    color: $primaryColor;
                }
            }
        }
    }
</style>
