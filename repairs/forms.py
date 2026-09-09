from django import forms
from .models import RepairGuide

class RepairGuideForm(forms.ModelForm):
    class Meta:
        model = RepairGuide
        fields = ['category', 'title', 'brand', 'symptom', 'solution_steps']
        widgets = {
            'category': forms.Select(attrs={'class': 'form-control'}),
            'title': forms.TextInput(attrs={'class': 'form-control', 'placeholder': 'Weka kichwa cha habari...'}),
            'brand': forms.TextInput(attrs={'class': 'form-control', 'placeholder': 'Mfano: Samsung, Tecno...'}),
            'symptom': forms.Textarea(attrs={'class': 'form-control', 'rows': 3, 'placeholder': 'Eleza tatizo...'}),
            'solution_steps': forms.Textarea(attrs={'class': 'form-control', 'rows': 5, 'placeholder': 'Eleza hatua za kutatua...'}),
        }
