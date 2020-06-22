<template>
    <article class="edit-atom-wrapper edit-detail" v-bkloading="{ isLoading }">
        <section class="inner-header">
            <div class="title"> {{ $t('store.微扩展编辑') }} </div>
        </section>

        <main class="edit-main">
            <bk-form ref="editForm" class="edit-service" label-width="125" :model="form">
                <bk-form-item class="wt660" :label="$t('store.微扩展名称')" :required="true" property="serviceName" :rules="[requireRule, numMax, nameRule]" ref="serviceName">
                    <bk-input v-model="form.serviceName" :placeholder="$t('store.请输入微扩展名称，不超过20个字符')"></bk-input>
                </bk-form-item>
                <bk-form-item class="wt660" :label="$t('store.扩展点')" :required="true" property="extensionItemList" :rules="[requireRule]" ref="extensionItemList">
                    <bk-select :placeholder="$t('store.请选择扩展点')"
                        class="service-item"
                        :scroll-height="300"
                        :clearable="true"
                        @toggle="getServiceList"
                        :loading="isServiceListLoading"
                        searchable
                        multiple
                        display-tag
                        v-model="form.extensionItemList">
                        <bk-option-group
                            v-for="(group, index) in serviceList"
                            :name="group.name"
                            :key="index">
                            <bk-option v-for="(option, key) in group.children"
                                :key="key"
                                :id="option.id"
                                :name="option.name"
                            >
                            </bk-option>
                        </bk-option-group>
                    </bk-select>
                </bk-form-item>
                <bk-form-item :label="$t('store.标签')" property="labelIdList" class="wt660">
                    <bk-tag-input v-model="form.labelIdList" :list="labelList" display-key="labelName" search-key="labelName" trigger="focus" :placeholder="$t('store.请选择标签')"></bk-tag-input>
                </bk-form-item>
                <bk-form-item :label="$t('store.简介')" property="summary" :required="true" :rules="[requireRule]" ref="summary">
                    <bk-input v-model="form.summary" :placeholder="$t('store.请输入简介')"></bk-input>
                </bk-form-item>
                <bk-form-item :label="$t('store.描述')" property="description">
                    <bk-radio-group v-model="form.descInputType">
                        <bk-radio value="MANUAL" class="mr21"> {{ $t('store.手动录入') }} </bk-radio>
                        <bk-radio value="FILE"> {{ $t('store.fromReadme') }} </bk-radio>
                    </bk-radio-group>
                    <mavon-editor class="service-remark-input"
                        v-if="form.descInputType === 'MANUAL'"
                        ref="mdHook"
                        v-model="form.description"
                        :toolbars="toolbars"
                        :external-link="false"
                        :box-shadow="false"
                        preview-background="#fff"
                        @imgAdd="uploadimg"
                    />
                </bk-form-item>
                <div class="version-msg">
                    <p class="form-title"> {{ $t('store.媒体信息') }} </p>
                    <hr class="cut-line">
                </div>
                <bk-form-item :label="$t('store.截图')">
                    <upload type="PICTURE"
                        :file-list.sync="imageList"
                        :limit="6"
                        :size="2"
                        :tip="$t('store.支持jpg、png、gif、svg格式，不超过6张，每张不超过2M')"
                    ></upload>
                </bk-form-item>
                <bk-form-item :label="$t('store.视频教程')">
                    <upload type="VIDEO"
                        :file-list.sync="videoList"
                        :limit="4"
                        :size="50"
                        :tip="$t('store.支持mp4、ogg、webm格式，不超过4个，每个不超过50M')"
                    ></upload>
                </bk-form-item>
                <bk-form-item>
                    <bk-button theme="primary" @click="saveService"> {{ $t('store.保存') }} </bk-button>
                    <bk-button theme="primary" @click="$router.replace({ name: 'serviceDetail' })"> {{ $t('store.取消') }} </bk-button>
                </bk-form-item>
                <select-logo ref="selectLogo" label="Logo" :form="form" type="SERVICE" :is-err="logoErr" right="25"></select-logo>
            </bk-form>
        </main>
    </article>
</template>

<script>
    import { mapActions } from 'vuex'
    import { toolbars } from '@/utils/editor-options'
    import selectLogo from '@/components/common/selectLogo'
    import upload from '@/components/upload'

    export default {
        components: {
            selectLogo,
            upload
        },

        data () {
            return {
                isLoading: false,
                form: JSON.parse(JSON.stringify(this.$store.state.store.currentService)),
                requireRule: {
                    required: true,
                    message: this.$t('store.必填项'),
                    trigger: 'blur'
                },
                numMax: {
                    validator: (val = '') => (val.length <= 20),
                    message: this.$t('store.字段不超过20个字符'),
                    trigger: 'blur'
                },
                nameRule: {
                    validator: (val) => (/^[\u4e00-\u9fa5a-zA-Z0-9-]*$/.test(val)),
                    message: this.$t('store.由汉字、英文字母、数字、连字符(-)组成，长度小于20个字符'),
                    trigger: 'blur'
                },
                serviceList: [],
                labelList: [],
                imageList: [],
                videoList: [],
                isServiceListLoading: false,
                toolbars
            }
        },

        created () {
            this.hackData()
            this.initData()
        },

        methods: {
            ...mapActions('store', [
                'requestServiceItemList',
                'requestServiceLabel',
                'requestUpdateServiceInfo'
            ]),

            successFileUpload ({ responseData: { data: mediaUrl } }, mediaType) {
                if (this.form.mediaInfoList) {
                    this.form.mediaInfoList.push({ mediaUrl, mediaType })
                } else {
                    this.form.mediaInfoList = [{ mediaUrl, mediaType }]
                }
            },

            hackData () {
                this.form.labelIdList = (this.form.labelList || []).map(label => label.id)
                this.form.description = this.form.description || this.$t('store.serviceMdDesc')
                this.$set(this.form, 'desType', this.form.desType || 'hand')
                const mediaList = this.form.mediaList || []
                this.imageList.push(...mediaList.filter(x => x.mediaType === 'PICTURE'))
                this.videoList.push(...mediaList.filter(x => x.mediaType === 'VIDEO'))
            },

            getServiceList (isExpand) {
                if (!isExpand) return
                const code = this.form.projectCode
                this.isServiceListLoading = true
                this.requestServiceItemList(code).then((res) => {
                    this.serviceList = (res || []).map((item) => {
                        const serviceItem = item.extServiceItem || {}
                        return {
                            name: serviceItem.name,
                            children: item.childItem || []
                        }
                    })
                }).catch((err) => this.$bkMessage({ message: err.message || err, theme: 'error' })).finally(() => (this.isServiceListLoading = false))
            },

            initData () {
                this.isLoading = true
                Promise.all([
                    this.requestServiceLabel(),
                    this.getServiceList(true)
                ]).then(([labels]) => {
                    this.labelList = labels || []
                }).catch((err) => this.$bkMessage({ message: err.message || err, theme: 'error' })).finally(() => {
                    this.isLoading = false
                })
            },

            saveService () {
                this.$refs.editForm.validate().then(() => {
                    if (!this.form.logoUrl) {
                        this.logoErr = true
                        const err = { field: 'selectLogo' }
                        throw err
                    }
                    this.isLoading = true
                    this.form.mediaList = [...this.imageList, ...this.videoList]
                    const postData = {
                        serviceCode: this.form.serviceCode,
                        data: this.form
                    }
                    this.requestUpdateServiceInfo(postData).then(() => {
                        return this.$store.dispatch('store/requestServiceDetailByCode', postData.serviceCode).then((res) => {
                            this.$store.dispatch('store/updateCurrentService', res || {})
                            this.$bkMessage({ message: this.$t('store.修改成功'), theme: 'success' })
                            this.$router.replace({ name: 'serviceDetail' })
                        })
                    }).catch((err) => this.$bkMessage({ message: err.message || err, theme: 'error' })).finally(() => {
                        this.isLoading = false
                    })
                }).catch((validate) => {
                    const field = validate.field
                    const label = this.$refs[field].label
                    this.$bkMessage({ message: `${label + this.$t('store.是必填项，请填写以后重试')}`, theme: 'error' })
                })
            },

            async uploadimg (pos, file) {
                const formData = new FormData()
                const config = {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                }
                let message, theme
                formData.append('file', file)

                try {
                    const res = await this.$store.dispatch('store/uploadFile', {
                        formData,
                        config
                    })

                    this.$refs.mdHook.$img2Url(pos, res)
                } catch (err) {
                    message = err.message ? err.message : err
                    theme = 'error'

                    this.$bkMessage({
                        message,
                        theme
                    })
                    this.$refs.mdHook.$refs.toolbar_left.$imgDel(pos)
                }
            }
        }
    }
</script>

<style lang="scss" scoped>
    @import './../../assets/scss/conf';

    .tag-list {
        padding: 0 20px;
        line-height: 32px;
        font-size: 12px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        color: #63656e;
    }

    .select-tag {
        min-height: 30px;
        &::after {
            content: '';
            display: table;
            clear: both;
        }
        li {
            float: left;
            height: 24px;
            background: #f1f2f3;
            border: 1px solid #d9d9d9;
            border-radius: 2px;
            line-height: 24px;
            margin: 3px 5px;
            padding: 0 4px;
            .icon-close {
                margin-right: 3px;
            }
        }
    }

    .dockerfile {
        height: 400px;
        overflow: auto;
        background: black;
        /deep/ .CodeMirror {
            font-family: Consolas, "Courier New", monospace;
            line-height: 1.5;
            padding: 10px;
            height: auto;
        }
    }

    .edit-atom-wrapper {
        height: 100%;
        overflow: auto;
        .inner-header {
            display: flex;
            justify-content: space-between;
            padding: 18px 20px;
            width: 100%;
            height: 60px;
            border-bottom: 1px solid $borderWeightColor;
            background-color: #fff;
            box-shadow:0px 2px 5px 0px rgba(51,60,72,0.03);
            .title {
                font-size: 16px;
            }
        }
    }

    .h32 {
        height: 32px;
    }

    .mt6 {
        margin-top: 6px;
    }

    .mr12 {
        margin-right: 12px;
    }

    .mr21 {
        margin-right: 21px;
    }

    .lh30 {
        line-height: 30px;
    }

    .wt660 {
        width: 660px;
    }

    .version-msg {
        margin: 30px 0 20px;
    }

    .edit-main {
        height: calc(100% - 60px);
        overflow: auto;
    }

    .edit-service {
        position: relative;
        max-width: 1200px;
        margin: 18px 20px;
        .service-remark-input {
            margin-top: 10px;
            height: 263px;
            border: 1px solid #c4c6cc;
            &.fullscreen {
                height: auto;
            }
        }
        .service-item {
            background-color: #fff;
        }
    }
</style>
