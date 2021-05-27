<template>
    <svg
        :width="size"
        :height="size"
        style="fill: currentColor"
    >
        <use v-bind="{ 'xlink:href': `#gitci-${name}` }" />
    </svg>
</template>

<script>
    export default {
        props: {
            name: String,
            size: {
                type: Number,
                default: 32
            }
        }
    }
</script>
