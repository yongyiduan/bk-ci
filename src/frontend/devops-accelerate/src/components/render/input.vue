<template>
    <bk-input v-if="isEdit" :value="paramValue[paramKey]" @change="changeParamValue" v-bind="$props" class="g-accelerate-single-width"></bk-input>
    <span v-else>{{ paramValue[paramKey] }}</span>
</template>

<script>
    import mixin from './mixins.js'

    export default {
        mixins: [mixin]
    }
</script>
