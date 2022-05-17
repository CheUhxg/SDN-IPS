all: p4ddos

p4ddos: c4DDoS.p4
	p4c-bm2-ss --arch v1model -o c4DDoS.json \
		--p4runtime-files C4DDos.p4info.txt \
		mytunnel.p4
