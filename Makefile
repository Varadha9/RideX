build:
	mvn -f common-library/pom.xml clean install -DskipTests -q
	mvn -f user-service/pom.xml clean package -DskipTests -q
	mvn -f driver-service/pom.xml clean package -DskipTests -q
	mvn -f booking-service/pom.xml clean package -DskipTests -q
	mvn -f trip-service/pom.xml clean package -DskipTests -q
	mvn -f payment-service/pom.xml clean package -DskipTests -q
	mvn -f pricing-service/pom.xml clean package -DskipTests -q
	mvn -f notification-service/pom.xml clean package -DskipTests -q
	mvn -f analytics-service/pom.xml clean package -DskipTests -q

up:
	docker-compose up -d --build

down:
	docker-compose down

restart:
	docker-compose down
	$(MAKE) build
	docker-compose up -d --build

logs:
	docker-compose logs -f

status:
	docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep ridex
