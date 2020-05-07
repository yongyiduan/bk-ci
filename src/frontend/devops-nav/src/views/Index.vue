<template>
    <div
        v-bkloading="loadingOption"
        class="devops-index"
    >
        <div class="user-prompt" v-if="showAnnounce">
            <!-- <p><i class="bk-icon icon-info-circle-shape"></i>{{currentNotice.noticeContent}}</p> -->
            <p v-html="currentNotice.noticeContent"></p>
        </div>
        <template v-if="projectList">
            <Header />
            <main>
                <template v-if="hasProjectList">
                    <empty-tips
                        v-if="!hasProject"
                        :title="$t('accessDeny.title')"
                        :desc="$t('accessDeny.desc')"
                    >
                        <bk-button
                            theme="primary"
                            @click="switchProject"
                        >
                            {{ $t('accessDeny.switchProject') }}
                        </bk-button>
                        <a
                            target="_blank"
                            class="empty-btns-item"
                            :href="`/console/perm/apply-join-project${$route.params.projectId ? `?project_code=${$route.params.projectId}` : ''}`"
                        >
                            <bk-button theme="success">{{ $t('apply') }}</bk-button>
                        </a>
                    </empty-tips>

                    <empty-tips
                        v-else-if="isDisableProject"
                        :title="$t('accessDeny.projectBan')"
                        :desc="$t('accessDeny.projectBanDesc')"
                    >
                        <bk-button
                            theme="primary"
                            @click="switchProject"
                        >
                            {{ $t('accessDeny.switchProject') }}
                        </bk-button>
                        <a
                            target="_blank"
                            class="empty-btns-item"
                            href="/console/pm"
                        ><bk-button theme="success">{{ $t("projectManage") }}</bk-button></a>
                    </empty-tips>

                    <!--<empty-tips v-else-if='isApprovalingProject' title='无法访问该项目' desc='你正在访问的项目正在处于审核中，禁止访问'>
                        <bk-button type='primary' @click='switchProject'>切换项目</bk-button>
                    </empty-tips>-->
                </template>
                <router-view v-if="!hasProjectList || isOnlineProject || isApprovalingProject" />
            </main>
        </template>

        <login-dialog v-if="showLoginDialog" />
        <ask-permission-dialog />
        <extension-aside-panel />
        <extension-dialog />
    </div>
</template>

<script lang="ts">
    import Vue from 'vue'
    import Header from '../components/Header/index.vue'
    import AskPermissionDialog from '../components/AskPermissionDialog/AskPermissionDialog.vue'
    import ExtensionAsidePanel from '../components/ExtensionAsidePanel/index.vue'
    import ExtensionDialog from '../components/ExtensionDialog/index.vue'
    import LoginDialog from '../components/LoginDialog/index.vue'
    import { Component } from 'vue-property-decorator'
    import { State, Getter, Action } from 'vuex-class'
    import eventBus from '../utils/eventBus'

    Component.registerHooks([
        'beforeRouteEnter',
        'beforeRouteLeave',
        'beforeRouteUpdate'
    ])

    @Component({
        components: {
            Header,
            LoginDialog,
            AskPermissionDialog,
            ExtensionAsidePanel,
            ExtensionDialog
        }
    })
    export default class Index extends Vue {
        @State currentNotice
        @State currentPage
        @State projectList
        @State headerConfig
        @State isShowPreviewTips
        @Getter enableProjectList
        @Getter disableProjectList
        @Getter approvalingProjectList
        @Action closePreviewTips
        @Action getAnnouncement
        @Action setAnnouncement
        @Action fetchServiceHooks

        showLoginDialog: boolean = false
        showExplorerTips: string = localStorage.getItem('showExplorerTips')

        get showAnnounce (): boolean {
            return this.currentNotice && this.currentNotice.id
        }

        get loadingOption (): object {
            return {
                isLoading: this.projectList === null
            }
        }

        get hasProject (): boolean {
            return this.projectList.some(project => project.projectCode === this.$route.params.projectId)
        }

        get isDisableProject (): boolean {
            const project = this.disableProjectList.find(project => project.projectCode === this.$route.params.projectId)
            return project ? !project.enabled : false
        }

        get isApprovalingProject (): boolean {
            return !!this.approvalingProjectList.find(project => project.projectCode === this.$route.params.projectId)
        }

        get isOnlineProject (): boolean {
            return !!this.enableProjectList.find(project => project.projectCode === this.$route.params.projectId)
        }

        get hasProjectList (): boolean {
            return this.headerConfig.showProjectList
        }

        get chromeExplorer (): boolean {
            const explorer = window.navigator.userAgent
            return explorer.indexOf('Chrome') >= 0 && explorer.indexOf('QQ') === -1
        }

        switchProject () {
            this.iframeUtil.toggleProjectMenu(true)
        }

        closeExplorerTips () {
            localStorage.setItem('showExplorerTips', 'false')
            this.closePreviewTips()
        }

        async created () {
            const announce = await this.getAnnouncement()
            if (announce && announce.id) {
                this.setAnnouncement(announce)
            }
            eventBus.$on('toggle-login-dialog', (isShow) => {
                this.showLoginDialog = isShow
            })

            if (this.showExplorerTips === null) {
                localStorage.setItem('showExplorerTips', 'true')
                this.showExplorerTips = localStorage.getItem('showExplorerTips')
            }
            eventBus.$on('update-project-id', projectId => {
                this.$router.replace({
                    params: {
                        projectId
                    }
                })
            })

            if (this.currentPage) {
                this.fetchServiceHooks({
                    serviceId: this.currentPage.id
                })
            }
        }
    }
</script>

<style lang="scss">
    @import '../assets/scss/conf';
    .devops-index {
        height: 100%;
        display: flex;
        flex: 1;
        flex-direction: column;
        background-color: $bgHoverColor;
        > main {
            display: flex;
            flex: 1;
            overflow: auto;
        }
        .user-prompt {
            display: flex;
            justify-content: center;
            padding: 0 24px;
            min-width: 1280px;
            line-height: 32px;
            background-color: #FF9600;
            color: #fff;
            .icon-info-circle-shape {
                position: relative;
                top: 2px;
                margin-right: 7px;
                font-size: 16px;
            }
            .close-remind {
                margin-right: 20px;
                cursor: pointer;
            }
            .icon-close {
                top: 8px;
                right: 24px;
                font-size: 14px;
                cursor: pointer;
            }
        }
    }
</style>
