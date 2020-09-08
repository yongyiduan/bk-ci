<template>
    <section class="show-version g-scroll-table">
        <bk-button theme="primary"
            class="version-button"
            :disabled="disableAddVersion"
            @click="$router.push({
                name: 'editService',
                params: {
                    serviceId: versionList[0].serviceId
                }
            })"
        > {{ $t('store.新增版本') }} </bk-button>
        <bk-table :data="versionList"
            :outer-border="false"
            :header-border="false"
            :header-cell-style="{ background: '#fff' }"
            :pagination="pagination"
            @page-change="(page) => $emit('pageChanged', page)"
            @page-limit-change="(currentLimit, prevLimit) => $emit('pageLimitChanged', currentLimit, prevLimit)"
        >
            <bk-table-column :label="$t('store.版本')" prop="version" :formatter="versionFormatter"></bk-table-column>
            <bk-table-column :label="$t('store.状态')" prop="serviceStatus" :formatter="statusFormatter"></bk-table-column>
            <bk-table-column :label="$t('store.创建人')" prop="creator"></bk-table-column>
            <bk-table-column :label="$t('store.创建时间')" prop="createTime" :formatter="convertTime"></bk-table-column>
            <bk-table-column :label="$t('store.操作')" width="120" class-name="handler-btn">
                <template slot-scope="props">
                    <section v-show="!index">
                        <span class="update-btn"
                            v-if="props.row.serviceStatus === 'INIT'"
                            @click="$router.push({ name: 'editService', params: { serviceId: props.row.serviceId } })"
                        > {{ $t('store.上架') }} </span>
                        <span class="update-btn"
                            v-if="progressStatus.indexOf(props.row.serviceStatus) > -1"
                            @click="$router.push({ name: 'serviceProgress', params: { serviceId: props.row.serviceId } })"
                        > {{ $t('store.进度') }} </span>
                    </section>
                </template>
            </bk-table-column>
        </bk-table>
    </section>
</template>

<script>
    import { serviceStatusMap } from '@/store/constants'
    import { convertTime } from '@/utils/index'

    export default {
        props: {
            versionList: Array
        },

        data () {
            return {
                progressStatus: ['AUDITING', 'COMMITTING', 'BUILDING', 'EDIT', 'BUILD_FAIL', 'TESTING', 'RELEASE_DEPLOYING', 'RELEASE_DEPLOY_FAIL'],
                upgradeStatus: ['AUDIT_REJECT', 'RELEASED', 'GROUNDING_SUSPENSION']
            }
        },

        computed: {
            disableAddVersion () {
                const firstVersion = this.versionList[0] || {}
                return this.upgradeStatus.indexOf(firstVersion.serviceStatus) === -1
            }
        },

        methods: {
            statusFormatter (row, column, cellValue, index) {
                return this.$t(serviceStatusMap[cellValue])
            },

            convertTime (row, column, cellValue, index) {
                return convertTime(cellValue)
            },

            versionFormatter (row, column, cellValue, index) {
                return cellValue || 'init'
            }
        }
    }
</script>
