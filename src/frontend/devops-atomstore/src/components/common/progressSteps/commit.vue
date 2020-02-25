<template>
    <section class="main-body">
        <bk-form class="progress-info">
            <bk-form-item :label="$t('store.可见范围')">
                <ul class="info-visable" v-if="commitInfo.deptInfoList.length">
                    <li v-for="dept in commitInfo.deptInfoList" :key="dept.deptId" class="visable-item">{{ dept.deptName }}<icon name="close" size="10" class="dept-close" @click.native="deleteDept(dept)" /></li>
                </ul>
                <p @click="showDialog = true" class="info-add-vis"><i class="bk-icon icon-plus"></i>{{ $t('store.添加') }}</p>
            </bk-form-item>
            <bk-form-item :label="$t('store.截图')">
                <bk-upload
                    :tip="$t('只允许上传JPG、PNG、JPEG的文件，文件最大为2MB')"
                    :with-credentials="true"
                    url="/support/api/user/file/upload"
                    name="file"
                    :header="{ 'Content-Type': 'multipart/form-data' }"
                    accept="image/png,image/jpeg,image/jpg"
                    :handle-res-code="res => res.status === 0"
                    size="2"
                    @on-success="(res) => successFileUpload(res, 'PICTURE')"
                ></bk-upload>
            </bk-form-item>
            <bk-form-item :label="$t('store.视频教程')">
                <bk-upload
                    :tip="$t('只允许上传video的文件，文件最大为500MB')"
                    :with-credentials="true"
                    url="/support/api/user/file/upload"
                    name="file"
                    :header="{ 'Content-Type': 'multipart/form-data' }"
                    accept="video/mp4,video/x-m4v,video/*"
                    :handle-res-code="res => res.status === 0"
                    size="500"
                    @on-success="(res) => successFileUpload(res, 'VIDEO')"
                ></bk-upload>
            </bk-form-item>
        </bk-form>

        <footer class="main-footer">
            <bk-button theme="primary" @click="submit" :loading="isCommiting"> {{ $t('store.提交') }} </bk-button>
            <bk-button @click="previousStep"> {{ $t('store.上一步') }} </bk-button>
        </footer>

        <organization-dialog :show-dialog="showDialog"
            :is-loading="false"
            @saveHandle="saveHandle"
            @cancelHandle="cancelHandle">
        </organization-dialog>
    </section>
</template>

<script>
    import organizationDialog from '@/components/organization-dialog'

    export default {
        components: {
            organizationDialog
        },

        props: {
            currentStep: {
                type: Object
            },
            detail: {
                type: Object
            }
        },

        data () {
            return {
                commitInfo: {
                    mediaInfoList: [],
                    deptInfoList: []
                },
                isCommiting: false,
                showDialog: false
            }
        },

        methods: {
            successFileUpload ({ responseData: { data: mediaUrl } }, mediaType) {
                this.commitInfo.mediaInfoList.push({ mediaUrl, mediaType })
            },

            deleteDept (dept) {
                const index = this.commitInfo.deptInfoList.findIndex((x) => (x.deptId === dept.deptId))
                this.commitInfo.deptInfoList.splice(index, 1)
            },

            saveHandle (params) {
                const deptInfos = params.deptInfos || []
                deptInfos.forEach((dept) => {
                    const index = this.commitInfo.deptInfoList.findIndex((x) => (x.deptId === dept.deptId))
                    if (index <= -1) this.commitInfo.deptInfoList.push(dept)
                })
                this.showDialog = false
            },

            cancelHandle () {
                this.showDialog = false
            },

            previousStep () {
                this.$parent.currentStepIndex--
            },

            submit () {
                this.isCommiting = true
                const postData = {
                    serviceCode: this.detail.serviceCode,
                    commitInfo: this.commitInfo
                }
                this.$store.dispatch('store/requestCommitServiceInfo', postData).catch((err) => {
                    this.$bkMessage({ message: (err.message || err), theme: 'error' })
                }).finally(() => (this.isCommiting = false))
            }
        }
    }
</script>

<style lang="scss" scoped>
    @import '@/assets/scss/conf.scss';

    .progress-info {
        width: 90%;
        height: 100%;
        overflow: auto;
    }

    .info-visable {
        .visable-item {
            float: left;
            height: 32px;
            background: #ebedf0;
            border-radius: 2px;
            font-size: 14px;
            text-align: left;
            color: #222222;
            line-height: 14px;
            padding: 9px;
            margin-right: 19px;
            margin-bottom: 8px;
            .dept-close {
                cursor: pointer;
                margin-left: 16px;
                opacity: 0.6;
                color: #6d738b;
                &:hover {
                    opacity: 1;
                    color: #1592ff;
                }
            }
        }
        &:after {
            content: ' ';
            display: table;
            clear: both;
        }
    }

    .info-add-vis {
        cursor: pointer;
        font-size: 14px;
        color: #1592ff;
        line-height: 30px;
        .bk-icon {
            margin-right: 4px;
        }
    }
</style>
