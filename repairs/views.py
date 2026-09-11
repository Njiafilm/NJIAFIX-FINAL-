from django.shortcuts import render, redirect
from django.http import JsonResponse
import json
import os  # <--- Hakikisha unaongeza hii juu kabisa kwenye faili kama haipo

from .models import DeviceCategory, RepairGuide


def matengenezo_page(request):
    categories = DeviceCategory.objects.all()
    return render(request, 'matengenezo.html', {"categories": categories})


def api_categories(request):
    cats = DeviceCategory.objects.all()
    data = [{"id": c.id, "name": c.name, "icon": c.icon_name} for c in cats]
    return JsonResponse({"categories": data})


def api_guides(request):
    category_id = request.GET.get("category_id")
    query = request.GET.get("q", "").strip()

    guides = RepairGuide.objects.all()
    if category_id:
        guides = guides.filter(category_id=category_id)
    if query:
        guides = guides.filter(title__icontains=query) | guides.filter(symptom__icontains=query)

    data = [{
        "id": g.id,
        "title": g.title,
        "brand": g.brand,
        "symptom": g.symptom,
        "solution_steps": g.solution_steps,
        "difficulty": g.get_difficulty_display(),
        "image": g.image.url if g.image else None,
    } for g in guides[:100]]
    return JsonResponse({"guides": data})


def ongeza_repair_guide(request):
    if request.method == 'POST':
        category_id = request.POST.get('category')
        title = request.POST.get('title')
        brand = request.POST.get('brand')
        symptom = request.POST.get('symptom')
        solution_steps = request.POST.get('solution_steps')
        difficulty = request.POST.get('difficulty', 'easy')
        image = request.FILES.get('image')

        category_obj = None
        if category_id:
            category_obj = DeviceCategory.objects.filter(id=category_id).first()

        RepairGuide.objects.create(
            category=category_obj,
            title=title,
            brand=brand,
            symptom=symptom,
            solution_steps=solution_steps,
            difficulty=difficulty,
            image=image
        )
        return redirect('matengenezo_page')

    categories = DeviceCategory.objects.all()
    return render(request, 'ongeza_repair_guide.html', {"categories": categories})


# <--- Weka hapa chini kabisa kwenye views.py --->
def execute_fix(request):
    if request.method == 'POST':
        data = json.loads(request.body)
        action = data.get('action')

        if action == 'flush_dns':
            # Inafanya Flush DNS kwenye kompyuta moja kwa moja
            os.system('ipconfig /flushdns')  # Kwa Windows
            return JsonResponse({'message': 'DNS imesafishwa kikamilifu kwenye PC!'})

        elif action == 'reboot':
            # Amri ya kuwasha upya huduma au kifaa
            return JsonResponse({'message': 'Amri ya kuanzisha upya imetumwa.'})

    return JsonResponse({'error': 'Haikuwezekana kutekeleza'}, status=400)
