<template>
    <article>
        <section class="show-detail">
            <img v-if="detail.logoUrl" :src="detail.logoUrl" class="detail-img">
            <ul :class="[{ 'overflow': !hasShowAll }, 'detail-items']" ref="detail">
                <li class="detail-item">
                    <span class="item-name">{{ detail.serviceName }}</span>
                </li>
                <li class="detail-item">
                    <span class="detail-label">{{ $t('store.标识') }}：</span>
                    <span>{{ detail.serviceCode }}</span>
                </li>
                <li class="detail-item">
                    <span class="detail-label">{{ $t('store.扩展点') }}：</span>
                    <span>{{ detail.itemName || '--' }}</span>
                </li>
                <li class="detail-item">
                    <span class="detail-label">{{ $t('store.调试项目') }}：</span>
                    <span>{{ detail.initProjectCode || '--' }}</span>
                </li>
                <li class="detail-item">
                    <span class="detail-label">{{ $t('store.功能标签') }}：</span>
                    <label-list :label-list="detail.labelList.map(x => x.labelName)"></label-list>
                </li>
                <li class="detail-item">
                    <span class="detail-label">{{ $t('store.简介') }}：</span>
                    <span>{{ detail.summary || '--' }}</span>
                </li>
                <li class="detail-item">
                    <span class="detail-label">{{ $t('store.详细描述') }}：</span>
                    <mavon-editor
                        :editable="false"
                        default-open="preview"
                        :subfield="false"
                        :toolbars-flag="false"
                        :external-link="false"
                        :box-shadow="false"
                        preview-background="#fff"
                        v-model="detail.description"
                    />
                </li>
                <li class="detail-item">
                    <span class="detail-label">{{ $t('store.媒体信息') }}：</span>
                    <media-list class="media-list" :list="detail.mediaList" v-if="detail.mediaList.length"></media-list>
                </li>
            </ul>
            <span class="summary-all" @click="hasShowAll = true" v-if="isOverDes && !hasShowAll"> {{ $t('展开全部') }} </span>
        </section>

        <section class="show-version">
            <span class="version-label">{{ $t('store.版本列表') }}</span>
            <bk-button theme="primary"
                class="version-button"
                :disabled="disableAddVersion"
                @click="$router.push({ name: 'editService', params: { serviceId: versionList[0].serviceId } })"
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
    </article>
</template>

<script>
    import { serviceStatusMap } from '@/store/constants'
    import { convertTime } from '@/utils/index'
    import labelList from '../../../labelList'
    import mediaList from '@/components/common/mediaList'

    export default {
        filters: {
            releaseFilter (value) {
                const local = window.devops || {}
                let res = ''
                switch (value) {
                    case 'NEW':
                        res = local.$t('store.初始化')
                        break
                    case 'INCOMPATIBILITY_UPGRADE':
                        res = local.$t('store.非兼容升级')
                        break
                    case 'COMPATIBILITY_UPGRADE':
                        res = local.$t('store.兼容式功能更新')
                        break
                    case 'COMPATIBILITY_FIX':
                        res = local.$t('store.兼容式问题修正')
                        break
                }
                return res
            }
        },

        components: {
            labelList,
            mediaList
        },

        props: {
            detail: Object,
            versionList: Array,
            pagination: Object
        },

        data () {
            return {
                progressStatus: ['COMMITTING', 'BUILDING', 'BUILD_FAIL', 'TESTING', 'AUDITING'],
                upgradeStatus: ['AUDIT_REJECT', 'RELEASED', 'GROUNDING_SUSPENSION'],
                isOverDes: false,
                hasShowAll: false
            }
        },

        computed: {
            disableAddVersion () {
                const firstVersion = this.versionList[0] || {}
                return this.upgradeStatus.indexOf(firstVersion.atomStatus) === -1
            }
        },

        mounted () {
            this.$nextTick(() => {
                this.isOverDes = this.$refs.detail.scrollHeight > 290
            })
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
