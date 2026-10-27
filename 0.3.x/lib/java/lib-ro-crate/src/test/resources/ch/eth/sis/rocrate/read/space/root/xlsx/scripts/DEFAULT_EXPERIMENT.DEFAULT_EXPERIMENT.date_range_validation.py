#   Copyright ETH 2025 Zürich, Scientific IT Services
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

def getRenderedProperty(entity, property):
    value = entity.property(property)
    if value is not None:
        return value.renderedValue()

def validate(entity, isNew):
    start_date = getRenderedProperty(entity, "START_DATE")
    end_date = getRenderedProperty(entity, "END_DATE")
    if start_date is not None and end_date is not None and start_date > end_date:
        return "End date cannot be before start date!"
