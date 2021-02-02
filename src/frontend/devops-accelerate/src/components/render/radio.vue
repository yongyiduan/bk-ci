<template>
    <bk-radio-group v-if="isEdit"
        :value="paramValue[paramKey]"
        @change="changeParamValue"
        v-bind="$props"
        class="g-accelerate-single-width"
    >
        <bk-radio :value="option.id" v-for="option in paramEnum" :key="option.id">{{ option.name }}</bk-radio>
    </bk-radio-group>
    <span v-else>{{ displayValue }}</span>
</template>

<script>
    import mixin from './mixins.js'

    export default {
        mixins: [mixin]
    }
</script>
