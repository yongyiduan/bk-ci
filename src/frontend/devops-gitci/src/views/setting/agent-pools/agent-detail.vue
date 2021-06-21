<template>
    <div class="node-detail-wrapper"
        v-bkloading="{
            isLoading: loading.isLoading,
            title: loading.title
        }">
        <content-header class="info-header">
            <div slot="left">
                <i class="devops-icon icon-arrows-left" @click="toNodeList"></i>
                <input type="text" class="bk-form-input display-name-input"
                    ref="nodeName"
                    v-if="editable"
                    maxlength="30"
                    name="nodeName"
                    v-validate="'required'"
                    v-model="nodeDetails.displayName"
                    @blur="saveName"
                    :class="{ 'is-danger': errors.has('nodeName') }" />
                <span class="header-text" v-if="!editable">{{ nodeDetails.displayName }}</span>
                <i class="devops-icon icon-edit" v-if="!editable && nodeDetails.canEdit" @click="editNodeName"></i>
            </div>
            <div slot="right" class="node-handle">
                <span class="copy-btn" @click="copyHandle">
                    {{ nodeDetails.os === 'WINDOWS' ? 'Copy download link' : 'Copy install command'}}
                </span>
                <span class="download-btn" v-if="nodeDetails.os === 'WINDOWS'" @click="downloadHandle">Download installation package</span>
                <i class="devops-icon icon-refresh" @click="refresh"></i>
            </div>
        </content-header>
        <div class="sub-view-port" v-show="showContent">
            <ul class="base-prototype-list">
                <li v-for="(entry, index) in basePrototypeList" :key="index">
                    <div class="info-title">{{ entry.name }}：</div>
                    <div class="info-value" :title="entry.value">{{ entry.value }}</div>
                </li>
            </ul>
            <node-overview-chart></node-overview-chart>
            <!-- <node-detail-tab></node-detail-tab> -->
        </div>
    </div>
</template>

<script>
    import { mapState } from 'vuex'
    import { bus } from '@/utils/bus'
    import { copyText } from '@/utils/util'
    import { setting } from '@/http'
    // import nodeDetailTab from '@/components/setting/node-detail-tab'
    import nodeOverviewChart from '@/components/setting/node-overview-chart'

    export default {
        components: {
            // nodeDetailTab,
            nodeOverviewChart
        },
        data () {
            return {
                nodeDetails: {},
                showContent: false,
                editable: false,
                basePrototypeList: [
                    { id: 'hostname', name: 'cpuName', value: '' },
                    { id: 'ip', name: 'IP', value: '' },
                    { id: 'ncpus', name: 'CPU', value: '' },
                    { id: 'memTotal', name: 'ram', value: '' },
                    { id: 'createdUser', name: 'owner', value: '' },
                    { id: 'osName', name: 'cpuName', value: '' }
                ],
                loading: {
                    isLoading: false,
                    title: ''
                }
            }
        },
        computed: {
            ...mapState(['projectId']),
            nodeHashId () {
                return this.$route.params.agentId
            },
            agentLink () {
                return this.nodeDetails.os === 'WINDOWS' ? this.nodeDetails.agentUrl : this.nodeDetails.agentScript
            }
        },
        watch: {
            projectId: async function (val) {
                this.$router.push({ name: 'envList' })
            },
            nodeDetails (val) {
                this.basePrototypeList.forEach(item => {
                    item.value = val[item.id]
                })
            }
        },
        async mounted () {
            this.requestNodeDetail()
        },
        methods: {
            toNodeList () {
                this.$router.push({ name: 'nodeList' })
            },
            async requestNodeDetail () {
                this.loading.isLoading = true

                setting.requestNodeDetail(this.projectId, this.nodeHashId).then((res) => {
                    console.log(res, 3242)
                    this.nodeDetails = res
                }).catch((err) => {
                    const message = err.message ? err.message : err
                    const theme = 'error'

                    this.$bkMessage({
                        message,
                        theme
                    })
                }).finally(() => {
                    console.log(this.nodeDetails, 2342111)
                    this.loading.isLoading = false
                    this.showContent = true
                })
            },
            copyHandle () {
                if (copyText(this.agentLink)) {
                    this.$bkMessage({
                        theme: 'success',
                        message: 'Copy successfully'
                    })
                }
            },
            downloadHandle () {
                window.location.href = this.nodeDetails.agentUrl
            },
            async saveName () {
                if (!this.nodeDetails.displayName) {
                    this.$bkMessage({
                        theme: 'error',
                        message: 'Please enter displayName'
                    })
                } else {
                    const params = {
                        displayName: this.nodeDetails.displayName.trim()
                    }
                    try {
                        await this.$store.dispatch('environment/updateDisplayName', {
                            projectId: this.projectId,
                            nodeHashId: this.nodeHashId,
                            params
                        })
                        this.editable = false
                    } catch (err) {
                        this.$bkMessage({
                            theme: 'error',
                            message: err.message ? err.message : err
                        })
                    }
                }
            },
            editNodeName () {
                this.editable = true
                this.$nextTick(() => {
                    this.$refs.nodeName.focus()
                })
            },
            refresh () {
                this.requestNodeDetail()
                bus.$emit('refreshEnv')
                bus.$emit('refreshBuild')
                bus.$emit('refreshAction')
                bus.$emit('refreshCharts')
            }
        }
    }
</script>

<style lang="postcss">
    @import '@/css/conf';
    .node-detail-wrapper {
        height: 100%;
        overflow: hidden;
        .info-header {
            .icon-edit {
                margin-left: 6px;
                cursor: pointer;
            }
            .icon-arrows-left {
                margin-right: 4px;
                cursor: pointer;
                color: $primaryColor;
                font-size: 16px;
                font-weight: 600;
            }
            .display-name-input {
                width: 300px;
            }
            .node-handle {
                color: $primaryColor;
                span {
                    margin-left: 10px;
                    cursor: pointer;
                }
            }
            .icon-refresh {
                margin-left: 10px;
                cursor: pointer;
            }
        }
        .base-prototype-list {
            display: flex;
            width: 100%;
            border: 1px solid #DDE4EB;
            background-color: #FFF;
            li {
                flex: 1;
                height: 76px;
                padding: 16px 20px;
                overflow: hidden;
                border-right: 1px solid #DDE4EB;
                color: $fontWeightColor;
                &:last-child {
                    border-right: none;
                }
                .info-title {
                    font-weight: bold;
                }
                .info-value {
                    width: 100%;
                    margin-top: 8px;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                }
            }
        }
    }
</style>
