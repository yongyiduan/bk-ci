<template>
    <bk-exception class="exception-wrap-item" type="building" v-if="projectRelationId">
        <span>权限升级中</span>
        <div class="text-wrap">
            权限中心功能升级中，暂不支持按流水线授权，请需要权限的同学主动申请
        </div>
    </bk-exception>
    <vertical-tab :tabs="tabs" v-else></vertical-tab>
</template>

<script>
    import VerticalTab from './VerticalTab'
    export default {
        name: 'auth-tab',
        components: {
            VerticalTab
        },
        props: {
            pipelineAuthority: Object,
            projectGroupAndUsers: Array,
            updateAuthority: Function
        },
        computed: {
            projectId () {
                return this.$route.params.projectId
            },
            pipelineId () {
                return this.$route.params.pipelineId
            },
            tabs () {
                return [{
                    id: 'role',
                    name: this.$t('settings.accordingRoles'),
                    component: 'AuthoritySetting',
                    componentProps: {
                        authList: this.pipelineAuthority.role,
                        projectGroupAndUsers: this.projectGroupAndUsers,
                        setType: 'role_code',
                        titleName: this.$i18n.locale === 'en-US' ? 'role_code' : 'role_name',
                        handleUpdate: this.generateUpdateAuthCb('role'),
                        isLoading: !this.pipelineAuthority.role
                    }
                }, {
                    id: 'func',
                    name: this.$t('settings.accordingFunc'),
                    component: 'AuthoritySetting',
                    componentProps: {
                        authList: this.pipelineAuthority.policy,
                        projectGroupAndUsers: this.projectGroupAndUsers,
                        setType: 'policy_code',
                        titleName: this.$i18n.locale === 'en-US' ? 'policy_code' : 'policy_name',
                        handleUpdate: this.generateUpdateAuthCb('policy'),
                        isLoading: !this.pipelineAuthority.policy
                    }
                }]
            }
        },
        methods: {
            generateUpdateAuthCb (name) {
                return value => {
                    this.updateAuthority(name, value)
                }
            }
        }
    }
</script>

<style lang="scss" scoped>
    .exception-wrap-item {
        margin-top: 150px;
        /deep/ .bk-exception-text {
            font-size: 20px;
        }
    }
</style>
