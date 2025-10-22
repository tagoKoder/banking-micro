# banking-micro

# 🏦 Banking Microservices (Client & Account)

Dos microservicios **Spring Boot 2.4.2 / Java 11**:
- **client** → CRUD de clientes  
- **account** → cuentas, transacciones y **reporte**

Incluye `docker-compose`, datos H2 y colección de Postman.

---

## ⚙️ Requisitos
- Docker Desktop

---

## 🚀 1) Levantar todo con Docker

```bash
# desde la raíz del repo
docker compose build --no-cache
docker compose up -d
```
### Servicios:

client → http://localhost:8081

account → http://localhost:8080

### Parar:

```bash
docker compose down --remove-orphans
```

## 2) Endpoints clave

### Client [`http://localhost:8081`]
```http
GET /api/clients/{id}
POST /api/clients
PUT /api/clients/{id}
PUT /api/clients/partial/{id}
DELETE /api/clients/{id}
```
### Account [http://localhost:8080]
```http
GET /api/accounts/{id}
POST /api/accounts
PUT /api/accounts/{id}
PUT /api/accounts/partial/{id}
DELETE /api/accounts/{id}
```

### Transactions [http://localhost:8080]
```http
GET /api/transactions/{id}
POST /api/transactions
Ejemplo JSON:json{ "accountId": 1, "type": "DEPOSIT|WITHDRAW", "amount": 200 }
```

### Reporte
Ejemplo:
```http
GET /api/reportes?clienteId=2&amp;fecha=2022-02-01,2022-02-15
```

## 🧪 3) Datos de ejemplo (precargados en H2)

### Clientes
- (1) Jose Lema  
- (2) Marianela Montalvo  
- (3) Juan Osorio  

### Cuentas
- 478758 (Ahorro, 2000) → Jose  
- 225487 (Corriente, 100) → Marianela  
- 495878 (Ahorros, 0) → Juan  
- 496825 (Ahorros, 540) → Marianela  
- 585545 (Corriente, 1000) → Jose  

### Movimientos
- 2022-02-05: Retiro 575 en 478758  
- 2022-02-10: Depósito 600 en 225487  
- 2022-02-09: Depósito 150 en 495878  
- 2022-02-08: Retiro 540 en 496825  

---

## ⚡ 4) Probar rápido (curl)

```bash
curl http://localhost:8081/api/clients
curl http://localhost:8080/api/accounts
curl "http://localhost:8080/api/reportes?clienteId=2&fecha=2022-02-01,2022-02-15"
```
## 🧪 5) Ejecutar tests con Docker (sin instalar Java/Maven)

### Windows PowerShell

```powershell
docker run --rm -v ${PWD}\client:/app  -w /app maven:3.9.6-eclipse-temurin-11 mvn -B test
docker run --rm -v ${PWD}\account:/app -w /app maven:3.9.6-eclipse-temurin-11 mvn -B test
```

## 📬 6) Postman

Para probar los microservicios de forma rápida y organizada, sigue estos pasos:

### 🗂️ Colección
Importa la colección ubicada en la raíz del proyecto:

Banking-Microservices.postman_collection.json

### 🌐 Environment
Crea un nuevo Environment en Postman con las siguientes variables:

```text
baseUrlClient   = http://localhost:8081
baseUrlAccount  = http://localhost:8080
clientId        = 2
start           = 2022-02-01
end             = 2022-02-15
```

Estas variables permiten parametrizar las peticiones y facilitar la ejecución de pruebas.

### Folders a ejecutar
Una vez configurado el entorno, ejecuta los siguientes folders dentro de la colección:
- Client
- Account
- Transactions

Cada uno contiene peticiones clave para validar el funcionamiento de los endpoints, incluyendo creación, consulta, actualización y generación de reportes.