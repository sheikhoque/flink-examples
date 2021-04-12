import os
import tarfile
import update_maxmind

def test_fetch_maxmind_db():
    CURR_DIR = os.path.dirname(os.path.realpath(__file__))

    tmpfile = update_maxmind.fetch_maxmind_db("GeoLite2-ASN", "9yUVRSI4GIHPBEfi", "tar.gz")
    tar = tarfile.open(fileobj=tmpfile, mode='r:gz')
    for m in tar.getmembers():
        if m.name.endswith(".mmdb"):
            _, filename = os.path.split(m.name)
            dbfile = open(filename, "wb")
            f = tar.extractfile(m)
            dbfile.write(f.read())
    tar.close()
