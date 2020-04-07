<template>
    <div class="bk-spin-loading bk-spin-loading-mini bk-spin-loading-primary store-status" v-if="status === 'doing'">
        <div class="rotate rotate1"></div>
        <div class="rotate rotate2"></div>
        <div class="rotate rotate3"></div>
        <div class="rotate rotate4"></div>
        <div class="rotate rotate5"></div>
        <div class="rotate rotate6"></div>
        <div class="rotate rotate7"></div>
        <div class="rotate rotate8"></div>
    </div>
    <icon class="store-status" :name="`store-status-${status}`" size="14" v-else />
</template>

<script>
    export default {
        props: {
            status: String
        }
    }
</script>

<style lang="scss" scoped>
    .store-status {
        vertical-align: text-top;
        &.bk-spin-loading {
            width: 14px;
            height: 14px;
        }
    }
</style>
