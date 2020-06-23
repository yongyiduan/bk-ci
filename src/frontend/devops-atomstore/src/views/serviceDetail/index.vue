<template>
    <div class="biz-container service-detail-wrapper" v-bkloading="{ isLoading }">
        <div class="biz-side-bar">
            <side-bar
                :nav="sideMenuNav"
                :side-menu-list="sideMenuList"
                :sub-system-name="'serviceLevel'">
            </side-bar>
        </div>
        <router-view style="width: 100%" v-if="!isLoading"></router-view>
    </div>
</template>

<script>
    import { mapGetters } from 'vuex'
    import sideBar from '@/components/side-nav'

    export default {
        components: {
            sideBar
        },
        data () {
            return {
                isLoading: true,
                sideMenuList: [
                    {
                        list: [
                            {
                                id: 'serviceOverview',
                                selectId: ['serviceOverview'],
                                name: this.$t('store.概览'),
                                icon: 'icon-overview',
                                showChildren: false
                            },
                            {
                                id: 'serviceDetail',
                                selectId: ['serviceDetail', 'serviceEdit'],
                                name: this.$t('store.详情'),
                                icon: 'icon-txt',
                                showChildren: false
                            },
                            {
                                id: 'serviceSettings',
                                selectId: ['serviceSettings'],
                                name: this.$t('store.设置'),
                                icon: 'icon-cog',
                                isOpen: false,
                                showChildren: true,
                                children: [
                                    {
                                        id: 'serviceMemberManage',
                                        selectId: ['serviceMemberManage'],
                                        name: this.$t('store.成员管理'),
                                        icon: ''
                                    },
                                    {
                                        id: 'serviceVisibleRange',
                                        selectId: ['serviceVisibleRange'],
                                        name: this.$t('store.可见范围'),
                                        icon: ''
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        },
        computed: {
            ...mapGetters('store', {
                'currentService': 'getCurrentService'
            }),
            routeName () {
                return this.$route.name
            },
            serviceCode () {
                return this.$route.params.serviceCode
            },
            sideMenuNav () {
                return {
                    backUrl: 'workList',
                    backType: 'service',
                    icon: 'atom-story',
                    title: this.currentService.serviceName,
                    url: ''
                }
            }
        },

        created () {
            this.initData()
        },

        methods: {
            goBack () {
                this.$router.push({
                    name: 'workList',
                    params: {
                        type: 'service'
                    }
                })
            },

            initData () {
                if (['serviceMemberManage'].includes(this.routeName)) {
                    this.sideMenuList[0].list[3].isOpen = true
                }
                Promise.all([this.getMemInfo(), this.requestServiceDetail()]).catch((err) => {
                    this.$bkMessage({ message: err.message || err, theme: 'error' })
                }).finally(() => (this.isLoading = false))
            },

            requestServiceDetail () {
                return this.$store.dispatch('store/requestServiceDetailByCode', this.serviceCode).then(res => this.$store.dispatch('store/updateCurrentService', res || {}))
            },

            getMemInfo () {
                return this.$store.dispatch('store/requestGetServiceMemInfo', this.serviceCode).then((res = {}) => {
                    const userInfo = {
                        isProjectAdmin: res.type === 'ADMIN',
                        userName: res.userName
                    }
                    if (!userInfo.isProjectAdmin) this.sideMenuList[0].list.splice(2, 1)
                    this.$store.dispatch('store/updateUserInfo', userInfo)
                })
            }
        }
    }
</script>

<style lang="scss">
    .service-detail-wrapper {
        min-width: 1200px;
        height: 100%;

        .bk-table {
            th:first-child,
            td:first-child {
                padding-left: 20px;
            }
        }
    }
    .sub-view-port {
        height: calc(100% - 60px);
        overflow: auto;
    }
    .disable {
        cursor: not-allowed !important;
        color: #dfe0e5 !important;
    }
</style>
