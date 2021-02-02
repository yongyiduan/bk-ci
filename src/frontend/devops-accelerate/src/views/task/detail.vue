<template>
    <article class="task-detail-home" v-bkloading="{ isLoading }">
        <bk-breadcrumb separator-class="bk-icon icon-angle-right" class="bread-crumb">
            <bk-breadcrumb-item :to="{ name: 'taskList' }">方案列表</bk-breadcrumb-item>
            <bk-breadcrumb-item>新增加速方案</bk-breadcrumb-item>
        </bk-breadcrumb>

        <section class="task-detail-body" v-if="!isLoading">
            <main class="task-detail-left">
                <task-basic :form-data.sync="formData" :only-edit="false" />
                <task-param :form-data.sync="formData" :only-edit="false" />
                <task-setting :form-data.sync="formData" :only-edit="false" />
            </main>

            <main class="task-detail-right">
                <section class="g-accelerate-box task-use">
                    <h3 class="create-title g-accelerate-deep-black-font">使用方式</h3>
                    <h5 class="tip-subtitle">方式一：</h5>
                    <span class="tip-word">在蓝盾流水线添加“编译加速”插件进行关联加速方案<a href="" class="g-accelerate-click-text">查看指引</a></span>
                    <h5 class="tip-subtitle">方式二：</h5>
                    <span class="tip-word">在自己的构建上使用：</span>
                    <span class="tip-word">1. 下载加速客户端安装包<a href="" class="g-accelerate-click-text">立即下载</a></span>
                    <span class="tip-word">2. 以 root 登录编译机，上传并解压安装包，执行 cd installer && ./installer.sh 进行安装</span>
                    <span class="tip-word">3. 修改编译命令，示例：bk-booster -bt cc -p 3ad33e69016443c8bda1e371ce0013b9 -a &quot;你的命令(如：make -j@BK_JOBS all)&quot; --hook<a href="" class="g-accelerate-click-text">查看指引</a></span>
                </section>

                <section class="g-accelerate-box task-record">
                    <h3 class="create-title g-accelerate-deep-black-font">更新记录</h3>
                    <bk-form :label-width="120">
                        <bk-form-item label="创建人：">
                            {{ formData.createdBy }}
                        </bk-form-item>
                        <bk-form-item label="创建时间：">
                            {{ formData.createdDate }}
                        </bk-form-item>
                        <bk-form-item label="最近修改人：">
                            {{ formData.updatedBy }}
                        </bk-form-item>
                        <bk-form-item label="修改时间：">
                            {{ formData.updatedDate }}
                        </bk-form-item>
                    </bk-form>
                </section>
            </main>
        </section>
    </article>
</template>

<script>
    import { getPlanDetailById } from '@/api'
    import taskBasic from '@/components/task/basic'
    import taskParam from '@/components/task/param'
    import taskSetting from '@/components/task/setting'

    export default {
        components: {
            taskBasic,
            taskParam,
            taskSetting
        },

        data () {
            return {
                formData: {},
                isLoading: false
            }
        },

        created () {
            this.getPlanDetail()
        },

        methods: {
            getPlanDetail () {
                const planId = this.$route.params.id
                this.isLoading = true
                getPlanDetailById(planId).then((res) => {
                    this.formData = res
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                })
            }
        }
    }
</script>

<style lang="scss" scoped>
    .task-detail-home {
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
    .task-detail-body {
        display: flex;
        .task-detail-left {
            flex: 874;
            margin-right: 20px;
        }
        .task-detail-right {
            flex: 360;
            line-height: 22px;
            .task-use {
                margin-bottom: 20px;
                padding: 26px 32px 67px;
            }
            .tip-subtitle {
                color: #979ba5;
                font-weight: normal;
                margin-top: 16px;
                font-size: 14px;
            }
            .tip-word {
                display: block;
                word-break: break-all;
                margin-bottom: 6px;
            }
            .g-accelerate-click-text {
                margin-left: 10px;
            }
            .task-record {
                padding: 26px 32px;
            }
        }
    }
    .create-title {
        font-size: 14px;
        line-height: 22px;
        margin-bottom: 17px;
    }
</style>
