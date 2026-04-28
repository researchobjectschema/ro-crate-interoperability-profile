from typing import List

from rocrate.model import ContextEntity
from rocrate.rocrate import ROCrate


def main():
    crate = ROCrate("rocrate.zip")
    entities = [x for x in crate.get_entities()]
    our_metadata: List[ContextEntity] = [x for x in entities if 'openbis:' in x.type]
    our_schema: List[ContextEntity] = [x for x in entities if 'rdfs:' in x.type.lower()]
    for stuff in our_schema:
        print(f'{stuff.id}: {stuff.as_jsonld()}')

    print("----\nmetadata\n----")

    for stuff in our_metadata:
        print(f'{stuff.id}: {stuff.as_jsonld()}')




if __name__ == '__main__':
    main()
