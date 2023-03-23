<template>
  <permission-main
    :resource-type="resourceType"
    :resource-code="projectCode"
    :project-code="projectCode"
  />
</template>

<script lang="ts">
import PermissionMain from '@/components/user-group/components/permission-main.vue';

export default {
  components: {
    PermissionMain,
  },

  data() {
    return {
      resourceType: 'project'
    };
  },

  computed: {
    projectCode() {
      return this.$route.params.projectCode;
    },
  },
};
</script>
