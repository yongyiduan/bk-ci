<template>
    <bk-radio-group :value="paramValue[paramKey]"
        @change="changeParamValue"
        v-bind="$props"
        class="g-accelerate-single-width"
    >
        <bk-radio :value="param.paramValue" :disabled="disabled" v-for="(param, index) in paramEnum" :key="index" class="g-accelerate-mr-10">{{ param.paramName }}</bk-radio>
    </bk-radio-group>
</template>

<script>
    import mixin from './mixins.js'

    export default {
        mixins: [mixin]
    }
</script>
