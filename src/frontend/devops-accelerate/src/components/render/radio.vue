<template>
    <bk-radio-group v-if="isEdit"
        :value="paramValue[paramKey]"
        @change="changeParamValue"
        v-bind="$props"
        class="g-accelerate-single-width"
    >
        <bk-radio :value="value" v-for="(key, value) in paramEnum" :key="key" class="g-accelerate-mr-10">{{ key }}</bk-radio>
    </bk-radio-group>
    <span v-else>{{ displayValue }}</span>
</template>

<script>
    import mixin from './mixins.js'

    export default {
        mixins: [mixin]
    }
</script>
