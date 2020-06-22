<template>
    <article class="service-detail-home" v-bkloading="{ isLoading }">
        <div class="inner-header">
            <div class="title"> {{ $t('store.微扩展详情') }} </div>
            <span @click="goToEditService" :class="[{ 'disable': !showEdit }, 'header-edit']" :title="!showEdit && $t('store.只有处于审核驳回、已发布、上架中止和已下架的状态才允许修改基本信息')"> {{ $t('store.编辑') }} </span>
        </div>
        <main class="detail-main">
            <bk-container flex :col="12">
                <bk-row>
                    <bk-col :span="9">
                        <bk-row>
                            <bk-col :span="4.5" class="g-progress-item">
                                <span class="g-progress-label">{{ $t('store.名称') }} :</span>
                                <span class="g-progress-content">{{currentService.serviceName}}</span>
                            </bk-col>
                            <bk-col :span="4.5" class="g-progress-item">
                                <span class="g-progress-label">{{ $t('store.标识') }} :</span>
                                <span class="g-progress-content">{{currentService.serviceCode}}</span>
                            </bk-col>
                        </bk-row>
                        <bk-row>
                            <bk-col :span="4.5" class="g-progress-item">
                                <span class="g-progress-label">{{ $t('store.扩展点') }} :</span>
                                <span class="g-progress-content">{{currentService.itemName || '--'}}</span>
                            </bk-col>
                            <bk-col :span="4.5" class="g-progress-item">
                                <span class="g-progress-label">{{ $t('store.调试项目') }} :</span>
                                <span class="g-progress-content">{{currentService.initProjectCode || '--'}}</span>
                            </bk-col>
                        </bk-row>
                        <bk-row>
                            <bk-col :span="9" class="g-progress-item">
                                <span class="g-progress-label">{{ $t('store.功能标签') }} :</span>
                                <section class="g-progress-content label-list">
                                    <span class="label-card" v-for="(label, index) in currentService.labelList" :key="index">{{ label.labelName }}</span>
                                </section>
                            </bk-col>
                        </bk-row>
                    </bk-col>
                    <bk-col :span="3">
                        <img v-if="currentService.logoUrl" :src="currentService.logoUrl" class="g-progress-image">
                    </bk-col>
                </bk-row>
                <bk-row>
                    <bk-col :span="12" class="g-progress-item">
                        <span class="g-progress-label">{{ $t('store.简介') }} :</span>
                        <span class="g-progress-content">{{currentService.summary || '--'}}</span>
                    </bk-col>
                </bk-row>
                <bk-row>
                    <bk-col :span="12" class="g-progress-item">
                        <span class="g-progress-label">{{ $t('store.详细描述') }} :</span>
                        <section class="g-progress-content">
                            <p :class="{ 'overflow': !isDropdownShow }" ref="edit">
                                <mavon-editor class="service-remark-input"
                                    ref="mdHook"
                                    v-model="currentService.description"
                                    :editable="false"
                                    :toolbars-flag="false"
                                    default-open="preview"
                                    :box-shadow="false"
                                    :subfield="false"
                                    preview-back-ground="#fafbfd"
                                />
                            </p>
                            <span class="toggle-btn" v-if="isOverflow" @click="isDropdownShow = !isDropdownShow">{{ isDropdownShow ? $t('store.收起') : $t('store.展开') }}
                                <i :class="['devops-icon icon-angle-down', { 'icon-flip': isDropdownShow }]"></i>
                            </span>
                        </section>
                    </bk-col>
                </bk-row>
                <bk-row>
                    <bk-col :span="12" class="g-progress-item service-media">
                        <span class="g-progress-label">{{ $t('store.媒体信息') }} :</span>
                        <media-list class="g-progress-content media-list" :list="currentService.mediaList" v-if="currentService.mediaList.length"></media-list>
                    </bk-col>
                </bk-row>
            </bk-container>
            <section class="version-content" v-if="!isLoading">
                <div class="version-info-header">
                    <div class="info-title"> {{ $t('store.版本列表') }} </div>
                    <button class="bk-button bk-primary"
                        type="button"
                        :disabled="upgradeStatus.indexOf((versionList[0] || {}).serviceStatus) === -1"
                        @click="$router.push({ name: 'editService', params: { serviceId: versionList[0].serviceId } })"
                    > {{ $t('store.新增版本') }} </button>
                </div>
                <bk-table style="margin-top: 15px;"
                    :data="versionList"
                    :pagination="pagination"
                    @page-change="pageChanged"
                    @page-limit-change="pageCountChanged"
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
        </main>
    </article>
</template>

<script>
    import { mapGetters } from 'vuex'
    import { serviceStatusMap } from '@/store/constants'
    import { convertTime } from '../../utils/index'
    import mediaList from '../../components/common/mediaList'

    export default {
        components: {
            mediaList
        },

        data () {
            return {
                isLoading: false,
                showEdit: false,
                versionList: [],
                pagination: {
                    current: 1,
                    count: 1,
                    limit: 10
                },
                progressStatus: ['COMMITTING', 'BUILDING', 'BUILD_FAIL', 'TESTING', 'AUDITING'],
                upgradeStatus: ['AUDIT_REJECT', 'RELEASED', 'GROUNDING_SUSPENSION'],
                isDropdownShow: false,
                isOverflow: false
            }
        },

        computed: {
            ...mapGetters('store', {
                'currentService': 'getCurrentService'
            })
        },

        created () {
            this.getVersionList()
        },

        mounted () {
            this.$nextTick(() => (this.isOverflow = this.$refs.edit.scrollHeight > 180))
        },

        methods: {
            getVersionList () {
                const postData = {
                    serviceCode: this.currentService.serviceCode,
                    page: this.pagination.current,
                    pageSize: this.pagination.limit
                }
                this.isLoading = true
                this.$store.dispatch('store/requestServiceVersionList', postData).then((res) => {
                    this.versionList = res.records || []
                    this.pagination.count = res.count
                    const lastestVersion = this.versionList[0] || {}
                    const lastestStatus = lastestVersion.serviceStatus
                    this.showEdit = ['AUDIT_REJECT', 'RELEASED', 'GROUNDING_SUSPENSION', 'UNDERCARRIAGED'].includes(lastestStatus)
                }).catch((err) => this.$bkMessage({ message: err.message || err, theme: 'error' })).finally(() => (this.isLoading = false))
            },

            goToEditService () {
                if (!this.showEdit) return
                this.$router.replace({ name: 'serviceEdit' })
            },

            pageCountChanged (currentLimit, prevLimit) {
                if (currentLimit === this.pagination.limit) return

                this.pagination.current = 1
                this.pagination.limit = currentLimit
                this.getVersionList()
            },

            pageChanged (page) {
                this.pagination.current = page
                this.getVersionList()
            },

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

<style lang="scss" scoped>
    @import '@/assets/scss/conf.scss';

    .service-detail-home {
        height: 100%;
        overflow: hidden;
        .detail-main {
            height: calc(100% - 60px);
            overflow: hidden auto;
            padding: 20px 0;
            .toggle-btn {
                margin-left: 117px;
                font-size: 12px;
                color: $primaryColor;
                text-align: right;
                cursor: pointer;
                .devops-icon {
                    display: inline-block;
                    margin-left: 2px;
                    transition: all ease 0.2s;
                    &.icon-flip {
                        transform: rotate(180deg);
                    }
                }
            }
            .overflow {
                max-height: 180px;
                overflow: hidden;
            }
        }
    }

    .label-card {
        float: left;
        margin-bottom: 4px;
        margin-right: 4PX;
        padding: 2px 7px;
        font-size: 12px;
        border: 1px solid $borderWeightColor;
        background-color: #F0F1F3;
        color: $fontColor;
    }

    .inner-header {
        display: flex;
        justify-content: space-between;
        padding: 0 20px;
        width: 100%;
        height: 60px;
        border-bottom: 1px solid $borderWeightColor;
        background-color: #fff;
        box-shadow:0px 2px 5px 0px rgba(51,60,72,0.03);
        .title {
            font-size: 16px;
            line-height: 59px;
        }
        .header-edit {
            font-size: 16px;
            line-height: 59px;
            color: $primaryColor;
            cursor: pointer;
        }
    }

    .version-content {
        padding: 0 20px;
    }

    .version-info-header {
        display: flex;
        justify-content: space-between;
        margin-top: 36px;
        .info-title {
            font-weight: bold;
            line-height: 2.5;
        }
    }
    .version-table {
        margin-top: 10px;
        border: 1px solid $borderWeightColor;
        tbody {
            background-color: #fff;
        }
        th {
            height: 42px;
            padding: 2px 10px;
            color: #333C48;
            font-weight: normal;
            &:first-child {
                padding-left: 20px;
            }
        }
        td {
            height: 42px;
            padding: 2px 10px;
            &:first-child {
                padding-left: 20px;
            }
            &:last-child {
                padding-right: 30px;
            }
        }
        .handler-btn {
            span {
                display: inline-block;
                margin-right: 20px;
                color: $primaryColor;
                cursor: pointer;
            }
        }
        .no-data-row {
            padding: 40px;
            color: $fontColor;
            text-align: center;
        }
    }
</style>
