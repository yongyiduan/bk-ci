<template>
    <article class="list-init-home">
        <main class="g-accelerate-box init-main">
            <section class="init-title">
                <h3 class="title-main g-accelerate-deep-black-font">欢迎使用 Turbo 加速方案</h3>
                <p class="title-main-content">Turbo 基于分布式编译、缓存、容器技术，旨在为用户提供高效、稳定、便捷的加速服务，提升研发效率。已经支持 Linux 下的 C/C++ 后台服务编译，Windows 下的 UE4 代码编译等多场景下的加速。</p>
                <h5 class="title-recommend g-accelerate-deep-black-font">为你推荐的加速模式</h5>
                <h5 class="recommend-task">
                    <p class="task-main">
                        <span class="task-title g-accelerate-deep-black-font">Disttask-cc</span>
                        <span class="task-desc g-accelerate-gray-font">自研引擎，Linux下C/C++后台服务编译加速，后台服务编译加速，可缩短 80% 左右的编译时间。</span>
                    </p>
                    <p class="task-buttons">
                        <bk-button text class="task-doc">查看文档</bk-button>
                        <bk-button class="task-use" @click="goToCreate">立即使用</bk-button>
                    </p>
                </h5>
                <h5 class="recommend-task">
                    <p class="task-main">
                        <span class="task-title g-accelerate-deep-black-font">Disttask-mix</span>
                        <span class="task-desc g-accelerate-gray-font">自研引擎，macOS下的Unity纹理渲染+代码编译加速，Unity代码编译加速和纹理渲染加速</span>
                    </p>
                    <p class="task-buttons">
                        <bk-button text class="task-doc">查看文档</bk-button>
                        <bk-button class="task-use" @click="goToCreate">立即使用</bk-button>
                    </p>
                </h5>
            </section>
            <img src="" class="init-img">
        </main>
    </article>
</template>

<script>
    export default {
        methods: {
            goToCreate () {
                this.$router.push({ name: 'taskCreate' })
            }
        }
    }
</script>

<style lang="scss" scoped>
    .list-init-home {
        padding: .2rem;
        width: 100%;
        height: calc(100vh - 5.96vh);
        margin: 0 auto;
        .init-main {
            height: 100%;
            display: flex;
            .init-img {
                flex: 38.1;
                background: #fafbfd
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
