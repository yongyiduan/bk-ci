<template>
    <article class="task-success-home">
        <bk-breadcrumb separator-class="bk-icon icon-angle-right" class="bread-crumb">
            <bk-breadcrumb-item :to="{ name: 'taskList' }">方案列表</bk-breadcrumb-item>
            <bk-breadcrumb-item>新增加速方案</bk-breadcrumb-item>
        </bk-breadcrumb>

        <section class="g-accelerate-box">
            <p class="success-tip">
                <i class="bk-icon check"></i>
                <span class="g-accelerate-black-font">加速方案提交成功</span>
            </p>

            <section class="task-tip">
                <h5 class="tip-title g-accelerate-black-font">方案创建后，可以通过两种途径使用：</h5>
                <h5 class="tip-subtitle g-accelerate-gray-font">方式一：</h5>
                <span class="tip-word">1、在蓝盾流水线添加 编译加速 插件，关联方案<a href="" class="g-accelerate-click-text">查看详细指引</a></span>
                <h5 class="tip-subtitle g-accelerate-gray-font">方式二：</h5>
                <span class="tip-word">在自己的构建上使用：</span>
                <span class="tip-word">1. 下载加速客户端安装包<a href="" class="g-accelerate-click-text">立即下载</a></span>
                <span class="tip-word">2. 以 root 登录编译机，上传并解压安装包，执行 cd installer && ./installer.sh 进行安装</span>
                <span class="tip-word">3. 修改编译命令，示例：bk-booster -bt cc -p 3ad33e69016443c8bda1e371ce0013b9 -a &quot;你的命令(如：make -j@BK_JOBS all)&quot; --hook<a href="" class="g-accelerate-click-text">查看指引</a></span>
            </section>
        </section>

        <bk-button theme="primary" @click="goToList">关闭</bk-button>
    </article>
</template>

<script>
    export default {
        methods: {
            goToList () {
                this.$router.push({
                    name: 'taskList'
                })
            }
        }
    }
</script>

<style lang="scss" scoped>
    @import '../../assets/scss/var.scss';
    h5 {
        font-weight: normal;
        font-size: 14px;
    }
    .task-success-home {
        padding: 10px 20px;
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
        .g-accelerate-box {
            margin-bottom: 20px;
        }
    }
    .success-tip {
        height: round(90px * $designToPx);
        padding: 0 .32rem;
        line-height: round(90px * $designToPx);
        font-size: 20px;
        border-bottom: 1px solid #f0f1f5;
    }
    .task-tip {
        padding: 26px 96px 34px;
        .tip-title {
            line-height: 20px;
            margin-bottom: -5px;
        }
        .tip-subtitle {
            line-height: 22px;
            margin-bottom: 6px;
            margin-top: 19px;
        }
        .tip-word {
            display: block;
            line-height: 22px;
            margin-bottom: 6px;
        }
        .g-accelerate-click-text {
            margin-left: 10px;
        }
    }
</style>
