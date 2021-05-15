<template>
    <router-view class="gitci-home"></router-view>
</template>

<script>
    export default {
        name: 'app'
    }
</script>

<style lang="postcss">
    @import './css/reset.css';
    @import './css/app.css';

    .gitci-home {
        font-family: -apple-system,PingFang SC,BlinkMacSystemFont,Microsoft YaHei,Helvetica Neue,Arial;
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
</style>
