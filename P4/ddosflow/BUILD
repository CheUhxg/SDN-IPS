COMPILE_DEPS = CORE_DEPS + [
    "//apps/p4-myflows/pipeconf:onos-apps-p4-ddos-pipeconf",
]

osgi_jar(
    deps = COMPILE_DEPS,s
)

BUNDLES = [
    "//apps/p4-tutorial/mytunnel:onos-apps-P4-DDos-ddosflow",
]

onos_app(
    app_name = "org.onosproject.p4ddos.ddosflow",
    category = "Traffic Engineering",
    description = "Provides forwarding between each pair of hosts and definde the ddos",
    included_bundles = BUNDLES,
    required_apps = [
        "org.onosproject.c4ddos.pipeconf",
    ],
    title = "C4 DDoS App",
    url = "http://onosproject.org",
)
