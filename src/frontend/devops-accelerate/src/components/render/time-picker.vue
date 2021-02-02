<template>
    <bk-time-picker :value="paramValue[paramKey]" v-if="isEdit" @change="changeParamValue" v-bind="$props" class="g-accelerate-single-width"></bk-time-picker>
    <span v-else>{{ paramValue[paramKey] }}</span>
</template>

<script>
    import mixin from './mixins.js'

    export default {
        mixins: [mixin]
    }
</script>
