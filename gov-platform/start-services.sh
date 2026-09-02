#!/bin/bash

# 政务平台开发环境启动脚本

echo "🚀 正在启动开发服务..."

# 启动容器
docker start mysql redis minio nacos

# 等待服务就绪
echo "⏳ 等待服务就绪..."
sleep 5

# 检查状态
echo ""
echo "📊 服务状态："
echo "----------------------------------------"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "mysql|redis|minio|nacos"
echo "----------------------------------------"
echo ""
echo "✅ 启动完成！"
echo ""
echo "服务地址："
echo "  MySQL:  localhost:3306 (root/password)"
echo "  Redis:  localhost:6379"
echo "  Nacos:  http://localhost:8848/nacos (nacos/nacos)"
echo "  MinIO:  http://localhost:9001 (minioadmin/minioadmin)"
