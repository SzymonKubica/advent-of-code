from abc import ABC, abstractmethod

class Solution(ABC):
    @abstractmethod
    def first_part(self, input_file: str):
        pass
    @abstractmethod
    def second_part(self, input_file: str):
        pass
