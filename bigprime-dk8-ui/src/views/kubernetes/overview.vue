<template>
  <div class="container-list">
    <Breadcrumb
      :items="['menu.kubernetes', 'menu.kubernetes.overview']"
      :title="clusterStore.clusterInfo.clusterName"
    />
    <div class="contain">
      <tiny-layout>
        <tiny-row>
          <tiny-col :span="2">
            <router-link to="/home/Kubernetes/Workloads/Pods">
              <div class="pie-title">Pods({{ PodsCount }})</div>
            </router-link>
            <div ref="podContainer" style="width: 100%; height: 200px"></div>
          </tiny-col>
          <tiny-col :span="2">
            <router-link to="/home/Kubernetes/Workloads/Deployments">
              <div class="pie-title">Deployments({{ DeploymentsCount }})</div>
            </router-link>
            <div ref="deploymentContainer" style="width: 100%; height: 200px"></div>
          </tiny-col>
          <tiny-col :span="2">
            <router-link to="/home/Kubernetes/Workloads/DaemonSets">
              <div class="pie-title">Daemon Sets({{ DaemonSetsCount }})</div>
            </router-link>
            <div ref="daemonSetContainer" style="width: 100%; height: 200px"></div>
          </tiny-col>
          <tiny-col :span="2">
            <router-link to="/home/Kubernetes/Workloads/StatefulSets">
              <div class="pie-title">Stateful Sets({{ StatefulSetsCount }})</div>
            </router-link>
            <div ref="statefulSetContainer" style="width: 100%; height: 200px"></div>
          </tiny-col>
          <tiny-col :span="2">
            <router-link to="/home/Kubernetes/Workloads/ReplicaSets">
              <div class="pie-title">Replica Sets({{ ReplicaSetsCount }})</div>
            </router-link>
            <div ref="replicaSetContainer" style="width: 100%; height: 200px"></div>
          </tiny-col>
          <tiny-col :span="2">
            <router-link to="/home/Kubernetes/Workloads/Jobs">
              <div class="pie-title">Jobs({{ JobsCount }})</div>
            </router-link>
            <div ref="jobsContainer" style="width: 100%; height: 200px"></div>
          </tiny-col>
        </tiny-row>
        <tiny-row>
          <tiny-col :span="6">
            <div ref="cpuContainer" style="width: 100%; height: 400px"></div>
          </tiny-col>
          <tiny-col :span="6">
            <div ref="memoryContainer" style="width: 100%; height: 400px"></div>
          </tiny-col>
        </tiny-row>
        <tiny-row>
          <tiny-col :span="12">
            <Events :height="510" :isShowBreadcrumb="false"></Events>
          </tiny-col>
        </tiny-row>
      </tiny-layout>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import HuiCharts from '@opentiny/huicharts'
import { TinyCol, TinyLayout, TinyRow } from '@opentiny/vue'
import Events from '@/views/kubernetes/event/index.vue'
import { Kubernetes } from '@/services/kubernetes/kubernetes'
import { useClusterStore } from '@/store/modules/cluster'

const podContainer = ref(null)
const cpuContainer = ref(null)
const memoryContainer = ref(null)
const deploymentContainer = ref(null)
const daemonSetContainer = ref(null)
const statefulSetContainer = ref(null)
const replicaSetContainer = ref(null)
const jobsContainer = ref(null)
const overviewData = ref([])
const PodsCount = ref(0)
const DeploymentsCount = ref(0)
const DaemonSetsCount = ref(0)
const StatefulSetsCount = ref(0)
const ReplicaSetsCount = ref(0)
const JobsCount = ref(0)
const cpuData = ref<any[]>([])
const memoryData = ref<any[]>([])

let chart = new HuiCharts()
const clusterStore = useClusterStore()
const initPodPieChart = (container, text, data) => {
  chart.init(container)
  let option = {
    theme: 'hdesign-light',
    type: 'circle',
    color: ['#078c07', '#9ea2a4'],
    label: {
      show: false
    },
    legend: {
      show: false
    },
    title: {
      text: text,
      top: '70%',
      itemGap: 5,
      textStyle: {
        fontSize: 14
      }
    },
    data: data
  }
  let chartType = 'PieChart'
  chart.setSimpleOption(chartType, option)
  chart.render()
}

const initCpuChart = (data: any) => {
  chart.init(cpuContainer.value)
  let chartOption = {
    theme: 'hdesign-light',
    padding: [50, 30, 50, 20],
    itemStyle: {
      barWidth: 80,
      barGap: '20%',
      color: '#078c07'
    },
    legend: {
      show: false
    },
    data: data,
    xAxis: {
      data: 'node'
    },
    yAxis: {
      name: 'CPU使用量(%)'
    }
  }
  let chartType = 'BarChart'
  chart.setSimpleOption(chartType, chartOption)
  chart.render()
}

const initMemoryChart = (data: any) => {
  chart.init(memoryContainer.value)
  let chartOption = {
    theme: 'hdesign-light',
    padding: [50, 30, 50, 20],
    itemStyle: {
      barWidth: 150,
      barGap: '20%',
      color: '#078c07'
    },
    legend: {
      show: true,
      icon: 'line'
    },
    data: data,
    xAxis: {
      data: 'node'
    },
    yAxis: {
      name: '内存使用量(%)'
    }
  }
  let chartType = 'LineChart'
  chart.setSimpleOption(chartType, chartOption)
  chart.render()
}

const initData = async () => {
  overviewData.value = await Kubernetes.OverviewService.getWorkloadOverview()
  overviewData.value.forEach((item: any) => {
    if (item.workload == 'Pods') {
      let data = [{ value: item.runningCount, name: 'Running' }]
      let text = 'Running：' + item.runningCount
      if (item.unRunningCount > 0) {
        text += '\nUnKnown' + item.unRunningCount
        data.push({ value: item.unRunningCount, name: 'UnRunning' })
      }
      PodsCount.value = item.runningCount + item.unRunningCount
      initPodPieChart(podContainer.value, text, data)
    }

    if (item.workload == 'Deployments') {
      let data = [{ value: item.runningCount, name: 'Running' }]
      let text = 'Running：' + item.runningCount
      if (item.unRunningCount > 0) {
        text += '\nUnKnown' + item.unRunningCount
        data.push({ value: item.unRunningCount, name: 'UnRunning' })
      }
      DeploymentsCount.value = item.runningCount + item.unRunningCount
      initPodPieChart(deploymentContainer.value, text, data)
    }

    if (item.workload == 'DaemonSets') {
      DaemonSetsCount.value = item.runningCount
      initPodPieChart(daemonSetContainer.value, 'Running：' + item.runningCount, [
        { value: item.runningCount, name: 'Running' }
      ])
    }

    if (item.workload == 'StatefulSets') {
      StatefulSetsCount.value = item.runningCount
      initPodPieChart(statefulSetContainer.value, 'Running：' + item.runningCount, [
        { value: item.runningCount, name: 'Running' }
      ])
    }

    if (item.workload == 'ReplicaSets') {
      ReplicaSetsCount.value = item.runningCount
      initPodPieChart(replicaSetContainer.value, 'Running：' + item.runningCount, [
        { value: item.runningCount, name: 'Running' }
      ])
    }

    if (item.workload == 'Jobs') {
      JobsCount.value = item.runningCount
      initPodPieChart(jobsContainer.value, 'Running：' + item.runningCount, [
        { value: item.runningCount, name: 'Running' }
      ])
    }
  })
}

const initNodeData = async () => {
  const response = await Kubernetes.NodeService.listNodes()
  const metrics = await Kubernetes.NodeService.getNodeMetrics()
  response.items.forEach((item: any) => {
    let node = {} as any
    node.name = item.metadata.name
    if (metrics) {
      let nodeName = item.metadata.name
      const find = metrics.find((metric: any) => metric.nodeName == nodeName)
      if (find) {
        let ct = item.status.capacity.cpu.number
        let mt = item.status.capacity.memory.number
        cpuData.value.push({ node: nodeName, value: ((find.cpu / ct) * 100).toFixed(2) })
        memoryData.value.push({ node: nodeName, value: ((find.memory / mt) * 100).toFixed(2) })
      }
    }
  })
  initCpuChart(cpuData.value)
  initMemoryChart(memoryData.value)
}

onMounted(() => {
  initData()
  initNodeData()
})
</script>

<style scoped lang="less">
.container-list {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
}

.contain {
  flex: 1 1 auto;
  margin: 8px 10px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 0 8px 8px rgba(169, 174, 184, 0.05);
  padding: 10px;

  .pie-title {
    text-align: center;
    margin-bottom: -25px;
    color: #1a78c4;
  }
}
</style>
