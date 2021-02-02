<template>
    <section class="g-accelerate-box task-setting">
        <h3 class="create-title g-accelerate-deep-black-font">设置</h3>
        <bk-form :label-width="120" :model="copyFormData">
            <bk-form-item label="IP白名单" property="ip">
                <template v-if="isEdit">
                    <bk-input type="textarea"
                        class="double-width"
                        :rows="3"
                        :maxlength="100"
                        v-model="copyFormData.whiteList">
                    </bk-input>
                </template>
                <span v-else class="g-accelerate-text-break">{{ formData.whiteList }}</span>
            </bk-form-item>
        </bk-form>
        <bk-button v-if="isEdit && !onlyEdit" theme="primary" class="g-accelerate-bottom-button" @click="save">保存</bk-button>
        <bk-button v-if="isEdit && !onlyEdit" class="g-accelerate-bottom-button" @click="cancel">取消</bk-button>
        <span class="g-accelerate-edit-button" @click="isEdit = true" v-if="!onlyEdit"><logo name="edit-small" size="20"></logo>编辑</span>
    </section>
</template>

<script>
    import { modifyTaskWhiteList } from '@/api'
    import logo from '@/components/logo'

    export default {
        components: {
            logo
        },

        props: {
            onlyEdit: {
                type: Boolean,
                default: true
            },
            formData: {
                type: Object
            }
        },

        data () {
            return {
                isEdit: this.onlyEdit,
                copyFormData: {},
                isLoadng: false
            }
        },

        created () {
            this.copyFormData = JSON.parse(JSON.stringify(this.formData))
        },

        methods: {
            save () {
                this.isLoadng = true
                modifyTaskWhiteList(this.copyFormData).then(() => {
                    this.$bkMessage({ theme: 'success', message: '修改成功' })
                    this.$emit('update:formData', this.copyFormData)
                    this.isEdit = false
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoadng = false
                })
            },

            cancel () {
                this.copyFormData = JSON.parse(JSON.stringify(this.formData))
                this.isEdit = false
            }
        }
    }
</script>

<style lang="scss" scoped>
    .task-setting {
        position: relative;
    }

    .g-accelerate-box {
        margin-bottom: 20px;
        padding: 26px 32px;
        .create-title {
            font-size: 14px;
            line-height: 22px;
            margin-bottom: 17px;
        }
    }
    .double-width {
        width: 7.1rem;
    }
</style>
