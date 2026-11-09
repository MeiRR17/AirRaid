import unittest
from pathlib import Path
from tempfile import TemporaryDirectory
from dependency_inventory import inventory


class DependencyInventoryTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.pom = Path(__file__).resolve().parents[1] / "pom.xml"

    def test_real_pom_resolves_property_versions(self):
        data = inventory(self.pom)
        versions = {x["artifactId"]: x["version"] for x in data["dependencies"]}
        self.assertEqual("5.11.4", versions["junit-jupiter"])
        self.assertEqual("21", data["java_release"])
        self.assertEqual(3, len(data["build_plugins"]))
        self.assertFalse(data["vulnerability_scan_performed"])

    def test_missing_property_fails_closed(self):
        with TemporaryDirectory() as temp:
            text = self.pom.read_text(encoding="utf-8")
            text = text.replace("<junit.version>5.11.4</junit.version>",
                                "<junit.version></junit.version>")
            output = Path(temp) / "pom.xml"
            output.write_text(text, encoding="utf-8")
            with self.assertRaises(ValueError):
                inventory(output)

    def test_dynamic_version_is_rejected(self):
        with TemporaryDirectory() as temp:
            text = self.pom.read_text(encoding="utf-8")
            text = text.replace("<junit.version>5.11.4</junit.version>",
                                "<junit.version>5.12.0-SNAPSHOT</junit.version>")
            output = Path(temp) / "pom.xml"
            output.write_text(text, encoding="utf-8")
            with self.assertRaises(ValueError):
                inventory(output)


if __name__ == "__main__":
    unittest.main()
