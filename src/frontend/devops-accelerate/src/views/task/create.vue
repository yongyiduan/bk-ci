<template>
    <article class="create-task-home">
        <bk-breadcrumb separator-class="bk-icon icon-angle-right" class="bread-crumb">
            <bk-breadcrumb-item :to="{ name: 'taskList' }">方案列表</bk-breadcrumb-item>
            <bk-breadcrumb-item>新增加速方案</bk-breadcrumb-item>
        </bk-breadcrumb>

        <task-basic :form-data.sync="formData" ref="basic" />

        <task-param :form-data.sync="formData" ref="param" />

        <task-setting :form-data.sync="formData" ref="setting" />

        <bk-button theme="primary" @click="submit">提交</bk-button>
        <bk-button @click="cancel">取消</bk-button>
    </article>
</template>

<script>
    import taskBasic from '@/components/task/basic'
    import taskParam from '@/components/task/param'
    import taskSetting from '@/components/task/setting'
    import { addTurboPlan } from '@/api'

    export default {
        components: {
            taskBasic,
            taskParam,
            taskSetting
        },

        data () {
            return {
                formData: {
                    engineCode: '',
                    openStatus: true,
                    configParam: {}
                },
                isLoading: false
            }
        },

        methods: {
            submit () {
                this.isLoading = true
                const basicComponent = this.$refs.basic
                const basicForm = basicComponent.copyFormData
                const paramForm = this.$refs.param.copyFormData
                const settingForm = this.$refs.setting.copyFormData

                basicComponent.$refs.createTask.validate().then(() => {
                    const postData = {
                        ...basicForm,
                        configParam: paramForm.configParam,
                        whiteList: settingForm.whiteList,
                        projectId: this.$route.params.projectId
                    }
                    addTurboPlan(postData).then(() => {
                        this.$bkMessage({ theme: 'success', message: '添加成功' })
                        this.$router.push({ name: 'taskSuccess' })
                    }).catch((err) => {
                        this.$bkMessage({ theme: 'error', message: err.message || err })
                    }).finally(() => {
                        this.isLoading = false
                    })
                }, (validator) => {
                    this.$bkMessage({ message: validator.content || validator, theme: 'error' })
                })
            },

            cancel () {
                this.$router.back()
            }
        }
    }
</script>

<style lang="scss" scoped>
    .create-task-home {
        padding: 10px 20px 28px;
        margin: 0 auto;
        .bread-crumb {
            font-size: 12px;
            margin-bottom: 10px;
            /deep/ .bk-breadcrumb-separator {
                font-size: 14px;
            }
            .bk-breadcrumb-item:last-child {
                color: #000;
            }
        }
    }
</style>
