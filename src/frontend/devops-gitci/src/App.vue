<template>
    <article class="gitci-home">
        <header class="gitci-header">
            <span class="header-info">
                <icon name="export-ci" size="44"></icon>
                <span class="ci-name">Tencent CI</span>
                <icon name="git" size="18" class="gray-icon"></icon>
                <span class="git-project-path">bkdevops-plugins-test/fayenodejstesa</span>
                <icon name="setting"
                    size="18"
                    :class="{ setting: true, 'gray-icon': $route.name === 'pipelines', 'blue-icon': $route.name !== 'pipelines' }"
                    @click.native="goToSetting"
                ></icon>
            </span>
            <span>admin</span>
        </header>
        <router-view class="gitci-main"></router-view>
    </article>
</template>

<script>
    export default {
        name: 'app',

        methods: {
            goToSetting () {
                this.$router.push({ name: 'basicSetting' })
            }
        }
    }
</script>

<style lang="postcss">
    @import './css/reset.css';
    @import './css/app.css';

    .gitci-home {
        width: 100%;
        height: 100vh;
        overflow: hidden;
        font-size: 14px;
        color: #63656e;
        ::-webkit-scrollbar-thumb {
            background-color: #c4c6cc !important;
            border-radius: 3px !important;
            &:hover {
                background-color: #979ba5 !important;
            }
        }
        ::-webkit-scrollbar {
            width: 6px !important;
            height: 6px !important;
        }
    }

    .gitci-header {
        height: 61px;
        padding: 0 20px;
        background: #182132;
        border-bottom: 1px solid #dde4eb;
        display: flex;
        align-items: center;
        justify-content: space-between;
        color: #f5f7fa;
        .header-info {
            display: flex;
            justify-content: center;
            align-items: center;
            .ci-name {
                display: inline-block;
                margin: 0 40px 0 7.8px;
                font-size: 20px;
                color: #f5f7fa;
            }
            .git-project-path {
                display: inline-block;
                margin: 0 8px;
                color: #f5f7fa;
            }
            .setting {
                cursor: pointer;
            }
            .gray-icon {
                color: #979ba5;
                font-weight: normal;
            }
            .blue-icon {
                color: #3a84ff;
            }
        }
    }

    .gitci-main {
        height: calc(100% - 61px);
    }
</style>
