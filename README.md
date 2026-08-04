# NRO-Service
Service to support nonrepudiation of origin in the CPF
## Run with Docker Compose

This repository contains a `docker-compose.yaml` that runs:
- NRO Service (`cpf-nro-service`)
- PostgreSQL for Trusted Party (`cpf-nro-postgres`)

### Prerequisites
1. Docker Engine + Docker Compose installed on the machine where you run the stack. Follow [this](https://docs.docker.com/get-started/get-docker/) instructions.

### Clone this repository

```bash
git clone git@github.com:Common-Provenance-Framework/NRO-Service.git
```

### Generate certificates
Run commands from the `NRO-Service` root directory.
#### Prepare certificates directories

```
mkdir -p certs/trusted
mkdir -p certs/service
```

#### Generate root CA

```
openssl ecparam -name prime256v1 -genkey -noout -out ./certs/ca.key
openssl req -x509 -new -key ./certs/ca.key -sha256 -days 3650 \
   -subj "/C=CZ/O=CPF/CN=cpf-root-ca" \
   -out ./certs/trusted/ca.pem
```

#### Generate NRO Service certificate (EC)
```
cat > certs/service/v3_nro.ext <<'EOF'
basicConstraints=critical,CA:FALSE
keyUsage=critical,digitalSignature
extendedKeyUsage=clientAuth
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
EOF

openssl ecparam -name prime256v1 -genkey -noout -out ./certs/service/nro.key

openssl req -new -key ./certs/service/nro.key \
   -subj "/C=CZ/O=CPF/CN=NRO-Service" \
   -out ./certs/service/nro.csr

openssl x509 -req -in ./certs/service/nro.csr \
   -CA ./certs/trusted/ca.pem -CAkey ./certs/ca.key -CAcreateserial \
   -out ./certs/service/nro.pem -days 825 -sha256 \
   -extfile certs/service/v3_nro.ext
```

#### Check your certificates
Optional sanity checks:
```
openssl x509 -in ./certs/service/nro.pem -noout -subject -issuer
openssl x509 -in ./certs/service/nro.pem -noout -text | grep "Public Key Algorithm"
openssl verify -CAfile ./certs/trusted/ca.pem ./certs/service/nro.pem
```

#### Clean certificates directory
Cleanup temporary files (CSRs, extension configs, serial files):
```
rm -f ./certs/service/nro.csr \
      ./certs/service/v3_nro.ext \
      ./certs/trusted/ca.srl
```

### Build and start
Once yors [certificates](#generate-certificates) are ready, run this command from the `NRO-Service` root directory.
```bash
docker compose up --build --detach
```

### Verify services

```bash
docker ps
curl http://localhost:8082/api/v1/info
docker logs -f cpf-nro-service
```
### Stop stack

```bash
docker compose down
```
If You want to remove all data volumes (PostgreSQL)

```bash
docker compose down -v
```