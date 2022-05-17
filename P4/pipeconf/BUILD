COMPILE_DEPS = CORE_DEPS + [
    "@minimal_json//jar",
    "//protocols/p4runtime/model:onos-protocols-p4runtime-model",
    "//protocols/p4runtime/api:onos-protocols-p4runtime-api",
    "//drivers/default:onos-drivers-default",
]

osgi_jar(
    deps = COMPILE_DEPS,
)

BUNDLES = [
    "//apps/p4-tutorial/pipeconf:onos-apps-P4-DDos-pipeconf",
]

onos_app(
    app_name = "org.onosproject.p4ddos.pipeconf",
    category = "Pipeconf",
    description = "Provides pipeconf for the ONOS-P4 to ddos",
    included_bundles = BUNDLES,
    required_apps = [
        "org.onosproject.drivers.p4runtime",
    ],
    title = "P4 DDoS Pipeconf",
    url = "http://onosproject.org",
)
