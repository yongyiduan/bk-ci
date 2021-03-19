<template>
    <article class="list-init-home" v-bkloading="{ isloading }">
        <main class="g-accelerate-box init-main">
            <section class="init-title">
                <h3 class="title-main g-accelerate-deep-black-font">欢迎使用 Turbo 加速方案</h3>
                <p class="title-main-content">Turbo 基于分布式编译、缓存、容器技术，旨在为用户提供高效、稳定、便捷的加速服务，提升研发效率。已经支持 Linux 下的 C/C++ 后台服务编译，Windows 下的 UE4 代码编译等多场景下的加速。</p>
                <h5 class="title-recommend g-accelerate-deep-black-font">为你推荐的加速模式</h5>
                <h5 class="recommend-task" v-for="(recommend, index) in recommendList" :key="index">
                    <p class="task-main">
                        <span class="task-title g-accelerate-deep-black-font">{{ recommend.engineName }}</span>
                        <span class="task-desc g-accelerate-gray-font">{{ recommend.recommendReason }}</span>
                    </p>
                    <p class="task-buttons">
                        <bk-button text class="task-doc" @click="goToDoc(recommend.docUrl)">查看文档</bk-button>
                        <bk-button class="task-use" @click="goToCreate(recommend.engineCode)">立即使用</bk-button>
                    </p>
                </h5>
            </section>
            <span class="init-img"></span>
        </main>
    </article>
</template>

<script>
    import { getRecommendList, getPlanList } from '@/api'

    export default {
        data () {
            return {
                isloading: false,
                recommendList: []
            }
        },

        computed: {
            projectId () {
                return this.$route.params.projectId
            }
        },

        watch: {
            projectId () {
                this.judgeHasPlan()
            }
        },

        created () {
            this.getRecommendList()
        },

        methods: {
            judgeHasPlan () {
                return getPlanList(this.projectId, 1).then((res) => {
                    if (res.turboPlanCount > 0) {
                        this.$router.replace({ name: 'overview' })
                    }
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isloading = false
                })
            },

            getRecommendList () {
                this.isloading = true
                getRecommendList().then((res) => {
                    this.recommendList = res || []
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isloading = false
                })
            },

            goToDoc (docUrl) {
                window.open(docUrl, '_blank')
            },

            goToCreate (engineCode) {
                this.$router.push({
                    name: 'taskCreate',
                    query: {
                        engineCode
                    }
                })
            }
        }
    }
</script>

<style lang="scss" scoped>
    .list-init-home {
        padding: .2rem;
        width: 100%;
        height: calc(100vh - 5.96vh - 50px);
        margin: 0 auto;
        .init-main {
            height: 100%;
            display: flex;
            .init-img {
                flex: 38.1;
                background: #fafbfd;
                background-image: url('../../assets/img/task-init.png');
                background-size: contain;
                background-repeat: no-repeat;
                background-position: center;
            }
        }
    }
    .init-title {
        flex: 54.9;
        padding: 62px 64px;
        .title-main {
            font-weight: normal;
            font-size: 18px;
            line-height: 25px;
            margin-bottom: 15px;
        }
        .title-main-content {
            font-size: 14px;
            line-height: 22px;
            margin-bottom: 54px;
        }
        .title-recommend {
            font-weight: normal;
            font-size: 16px;
            line-height: 22px;
            margin-bottom: 15px;
        }
        .recommend-task {
            font-weight: normal;
            background: #f5f6fa;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 18px 20px;
            margin-bottom: 20px;
            .task-main {
                width: 367px;
            }
            .task-title {
                font-size: 16px;
                line-height: 24px;
                display: block;
                margin-bottom: 4px;
            }
            .task-desc {
                font-size: 12px;
                line-height: 20px;
            }
            .task-buttons {
                display: flex;
                align-items: center;
            }
            .task-use {
                margin-left: 15px;
                border-radius: 16px;
                border: 1px solid #F0F1F5;
                color: #3a84ff;
            }
        }
    }
</style>
